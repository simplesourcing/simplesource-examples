package io.simplesource.example.demo.projections;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.Gson;
import io.simplesource.example.demo.Config;
import io.simplesource.example.demo.repository.write.simplesource.Account;
import io.simplesource.example.demo.repository.write.simplesource.AccountCommand;
import io.simplesource.example.demo.repository.write.simplesource.AccountEvent;
import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.model.ValueWithSequence;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.*;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ElasticsearchProjectionService {
    private static final Duration POLL_DURATION = Duration.ofSeconds(30);

    private final Config config;

    private boolean isRunning = false;

    private AggregateSerdes<String, AccountCommand, AccountEvent, Optional<Account>> serdes;

    private static final String INDEX = "simplesourcedemo";


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
        private final RestHighLevelClient esClient;
        private final Gson gson = new Gson();


        private AtomicBoolean shutdown = new AtomicBoolean(false);


        private final Logger log = LoggerFactory.getLogger(EventLogConsummer.class);

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


            esClient = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(config.elasticsearchHost, config.elasticsearchPort, "http")));
        }

        @Override
        public void run() {
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafkaProps);

            log.info("****************************************");
            log.info("Started Elasticsearch projection service");
            log.info("****************************************");


            consumer.subscribe(Collections.singleton("account-event"));

            while (!shutdown.get()) {
                ConsumerRecords<String, String> records = consumer.poll(POLL_DURATION);
                try {
                    records.forEach(record -> copyToES(record));
                    consumer.commitSync();
                } catch (Exception e) {
                    log.error("Error creating projections", e);
                }
            }

            consumer.close(Duration.ofSeconds(30));

            try {
                esClient.close();
            } catch (IOException e) {

            }
        }

        private void copyToES(ConsumerRecord<String, String> record) {
            ValueWithSequence<AccountEvent> event = serdes.valueWithSequence().deserializer().deserialize(record.topic(), record.value().getBytes());
            String key = serdes.aggregateKey().deserializer().deserialize(record.topic(), record.key().getBytes());

            event.value().match(
                    accountCreated -> {
                        try {
                            Map<String, Object> data = new HashMap<>();
                            data.put("accountName", accountCreated.accountName);
                            data.put("balance", accountCreated.openingBalance);
                            data.put("sequence", event.sequence().getSeq());

                            IndexRequest req = new IndexRequest("simplesourcedemo")
                                    .type("account")
                                    .id(key)
                                    .source(data);


                            IndexResponse resp = esClient.index(req, RequestOptions.DEFAULT);
                            log.info(key + " index status: " + resp.getResult().name());
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }

                        return null;
                    },
                    deposited -> {
                        log.info("*****************{}**************", deposited.amount);
                        try {
                            // 1) Get current doc
                            GetRequest getRequest = new GetRequest(INDEX, key).type("account");
                            GetResponse getResponse = esClient.get(getRequest, RequestOptions.DEFAULT);

                            if(getResponse.isExists()) {
                                Map<String, Object> source = getResponse.getSource();
                                long sequence = Long.valueOf(source.get("sequence").toString()); // ES casts as a int so parse back as a long, hack should create an index schema

                                if(event.sequence().getSeq() == sequence+1) {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("balance",(double) source.get("balance") + deposited.amount);
                                    data.put("sequence", event.sequence().getSeq());

                                    UpdateRequest request = new UpdateRequest(INDEX, key)
                                            .type("account")
                                            .doc(data);

                                    UpdateResponse resp = esClient.update(request, RequestOptions.DEFAULT);
                                    log.info("Updated AccountSummary projection with id {}, new sequence: {}. {}", key, event.sequence().getSeq(), resp);
                                } else {
                                    log.error("Skipping deposited event projection with [{},{}]. Sequence miss-match", key, event.sequence().getSeq());
                                }

                            } else {
                                log.error("Skipping deposited event projection with [{},{}]. Existing entity not found", key, event.sequence().getSeq());
                            }

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return null;
                    },
                    withdrawn -> {
                        try {
                            // 1) Get current doc
                            GetRequest getRequest = new GetRequest(INDEX, key).type("account").version(event.sequence().getSeq() - 1);
                            GetResponse getResponse = esClient.get(getRequest, RequestOptions.DEFAULT);

                            // 2) Validate current doc version == sequenceNo - 1
                            if(getResponse.isExists()) {
                                Map<String, Object> source = getResponse.getSource();
                                long sequence = Long.valueOf(source.get("sequence").toString()); // ES casts as a int so parse back as a long, hack should create an index schema

                                if(event.sequence().getSeq() == sequence+1) {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("balance",(double) source.get("balance") - withdrawn.amount);
                                    data.put("sequence", event.sequence().getSeq());

                                    UpdateRequest request = new UpdateRequest(INDEX, key)
                                            .type("account")
                                            .doc(data);

                                    UpdateResponse resp = esClient.update(request, RequestOptions.DEFAULT);
                                    log.info("Updated AccountSummary projection with id {}, new sequence: {}. {}", key, event.sequence().getSeq(), resp);
                                } else {
                                    log.error("Skipping deposited event projection with [{},{}]. Sequence miss-match", key, event.sequence().getSeq());
                                }

                            } else {
                                log.error("Skipping deposited event projection with [{},{}]. Existing entity not found", key, event.sequence().getSeq());
                            }

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return null;
                    }
            );
        }
    }


}
