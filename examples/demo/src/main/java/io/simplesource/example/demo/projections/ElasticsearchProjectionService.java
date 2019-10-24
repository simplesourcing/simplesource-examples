package io.simplesource.example.demo.projections;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import io.simplesource.example.demo.Config;
import io.simplesource.example.demo.repository.write.simplesource.Account;
import io.simplesource.example.demo.repository.write.simplesource.AccountCommand;
import io.simplesource.example.demo.repository.write.simplesource.AccountEvent;
import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.model.ValueWithSequence;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.*;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ElasticsearchProjectionService {
    private static final Duration POLL_DURATION = Duration.ofSeconds(30);

    private final Config config;

    private boolean isRunning = false;

    private AggregateSerdes<String, AccountCommand, AccountEvent, Optional<Account>> serdes;


    public ElasticsearchProjectionService(Config config, AggregateSerdes<String, AccountCommand, AccountEvent, Optional<Account>> serdes) {
        this.config = config;
        this.serdes = serdes;
    }

    public void start() {
        if (!isRunning) {
            Thread consumer = new Thread(new EventLogConsummer(config));
            consumer.setPriority(Thread.MIN_PRIORITY);
            consumer.start();
            isRunning = true;
        }
    }


    private final class EventLogConsummer implements Runnable {
        private final Properties kafkaProps;

        private AtomicBoolean shutdown = new AtomicBoolean(false);


        private final Logger log = LoggerFactory.getLogger(EventLogConsummer.class);

        private final List<Indexer> indexers;


        public EventLogConsummer(Config config) {
            kafkaProps = new Properties();
            kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafkaBootstrapServers);
            kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, config.kafkaGropId + "projection-service");
            kafkaProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
            kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
            kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                shutdown.set(true);
            }));


            RestHighLevelClient esClient = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(config.elasticsearchHost, config.elasticsearchPort, "http")));

            indexers = List.of(
                    new AccountSummaryProjection(esClient),
                    new AccountTransactionProjection(esClient)
            );
        }

        @Override
        public void run() {
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafkaProps);

            log.info("****************************************");
            log.info("Started Elasticsearch projection service");
            log.info("****************************************");


            consumer.subscribe(Collections.singleton("account-event"));

            // Naive implementation, no error handling, will die on first exception. TODO
            while (!shutdown.get()) {
                ConsumerRecords<String, String> records = consumer.poll(POLL_DURATION);
                records.forEach(record -> {
                    ValueWithSequence<AccountEvent> event = serdes.valueWithSequence().deserializer().deserialize(record.topic(), record.value().getBytes());
                    String key = serdes.aggregateKey().deserializer().deserialize(record.topic(), record.key().getBytes());
                    indexers.stream().forEach(indexer -> indexer.index(key, event));
                });
                consumer.commitSync();
            }

            consumer.commitSync();
            consumer.close(Duration.ofSeconds(60));
        }


    }
}
