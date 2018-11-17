package io.simplesource.example.auction.account.query.projection;

import io.simplesource.example.auction.account.avro.AccountAvroMappers;
import io.simplesource.example.auction.account.command.AccountMappedAggregate;
import io.simplesource.example.auction.account.domain.*;
import io.simplesource.example.auction.account.domain.AccountEvents.AccountEvent;
import io.simplesource.kafka.dsl.KafkaConfig;
import io.simplesource.kafka.model.ValueWithSequence;
import io.simplesource.kafka.spec.AggregateSpec;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

import static com.google.common.collect.ImmutableList.of;
import static io.simplesource.example.auction.RestApplication.accountResourceNamingStrategy;
import static io.simplesource.example.auction.account.query.projection.AccountProjectionAggregator.accountTransactionEventAggregator;
import static io.simplesource.example.auction.account.query.projection.AccountProjectionSpecBuilder.buildWithSequence;
import static io.simplesource.kafka.util.KafkaStreamsUtils.*;
import static java.util.Objects.nonNull;

public final class AccountProjectionStreamApp {
    private static final Logger logger = LoggerFactory.getLogger(AccountProjectionStreamApp.class);
    public static final String ACCOUNT_AGGREGATE_NAME = "account";
    private static final String ACCOUNT_EVENT_TOPIC_NAME = "account_avro_account-event";
    private static final String PROJECTION_APP_ID = "account_projection_app";
    public static final String SCHEMA_REGISTRY_URL = "http://schema_registry:8081";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String APPLICATION_SERVER = "localhost:1234";
    private static final String ACCOUNT_TRANSACTIONS_PROJECTION_TOPIC = "auction_account_transactions_projection";
    private static final String ACCOUNT_PROJECTION_TOPIC = "auction_account_projection";

    public static final AggregateSpec<AccountKey, ?, AccountEvent, ?> ACCOUNT_AGGREGATE_SPEC = accountAggregateSpec();

    private KafkaStreams streams = null;

    public synchronized void start() {
        if (nonNull(streams)) throw new IllegalStateException("Application already started");

        final Topology topology = buildTopology();
        streams = startApp(topology);
        waitUntilStable(logger, streams);
    }

    private KafkaStreams startApp(final Topology topology) {
        logger.info("Topology description {}", topology.describe());

        Properties kafkaStreamProperties = new Properties();
        kafkaStreamProperties.putAll(getKafkaConfig().streamsConfig());

        final KafkaStreams streams = new KafkaStreams(topology, kafkaStreamProperties);
        registerExceptionHandler(logger, streams);
        addShutdownHook(logger, streams);
        streams.start();

        return streams;
    }

    private Topology buildTopology() {
        final StreamsBuilder builder = new StreamsBuilder();

        KStream<AccountKey, ValueWithSequence<AccountEvent>> accountEventStream = accountEventStream(builder);
        streamProjectionTopologies().forEach(t -> t.addTopology(accountEventStream));

        return builder.build();
    }

    private static List<KStreamProjectionTopology> streamProjectionTopologies() {
        ProjectionSpec<AccountKey, ValueWithSequence<Optional<Account>>> accountProjectionSpec = buildWithSequence(ACCOUNT_EVENT_TOPIC_NAME,
                ACCOUNT_PROJECTION_TOPIC, AccountAvroMappers.keyMapper,
                AccountProjectionAvroMappers.ACCOUNT_PROJECTION_MAPPER);

        ProjectionSpec<AccountTransactionKey, Optional<Reservation>> accountTransactionProjectionSpec =
                AccountProjectionSpecBuilder.build(ACCOUNT_EVENT_TOPIC_NAME,
                        ACCOUNT_TRANSACTIONS_PROJECTION_TOPIC, AccountProjectionAvroMappers.ACCOUNT_TRANSACTION_KEY_MAPPER,
                        AccountProjectionAvroMappers.ACCOUNT_TRANSACTION_PROJECTION_MAPPER);


        return of(new AccountProjectionStreamTopology(accountProjectionSpec, AccountEvents.getAggregator()),
                new AccountTransactionProjectionStreamTopology(accountTransactionProjectionSpec, accountTransactionEventAggregator()));
    }

    private KStream<AccountKey, ValueWithSequence<AccountEvent>> accountEventStream(StreamsBuilder builder) {
        return builder.stream(ACCOUNT_EVENT_TOPIC_NAME,
                Consumed.with(ACCOUNT_AGGREGATE_SPEC.serialization().serdes().aggregateKey(),
                        ACCOUNT_AGGREGATE_SPEC.serialization().serdes().valueWithSequence()));
    }

    private static KafkaConfig getKafkaConfig() {
        Function<KafkaConfig.Builder, KafkaConfig> builderFunc = builder ->
                builder
                        .withKafkaApplicationId(PROJECTION_APP_ID)
                        .withKafkaBootstrap(BOOTSTRAP_SERVERS)
                        .withApplicationServer(APPLICATION_SERVER)
                        .build();
        return builderFunc.apply(new KafkaConfig.Builder());
    }

    private static AggregateSpec<AccountKey, ?, AccountEvent, ?> accountAggregateSpec() {
        return AccountMappedAggregate.createSpec(
                ACCOUNT_AGGREGATE_NAME,
                AccountAvroMappers.createDomainSerializer(SCHEMA_REGISTRY_URL),
                accountResourceNamingStrategy(),
                (k) -> Optional.empty());
    }
}
