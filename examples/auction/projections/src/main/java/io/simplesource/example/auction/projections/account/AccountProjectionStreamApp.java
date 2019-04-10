package io.simplesource.example.auction.projections.account;

import com.google.common.collect.ImmutableList;
import io.simplesource.example.auction.avro.AccountAvroMappers;
import io.simplesource.example.auction.command.AccountCommand;
import io.simplesource.example.auction.domain.Account;
import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.example.auction.domain.AccountTransactionKey;
import io.simplesource.example.auction.domain.Reservation;
import io.simplesource.example.auction.event.AccountEvent;
import io.simplesource.example.auction.projections.Projector;
import io.simplesource.example.auction.projections.spec.EventStreamSpec;
import io.simplesource.example.auction.projections.spec.ProjectionSpec;
import io.simplesource.example.auction.projections.spec.ProjectionSpecBuilder;
import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.dsl.KafkaConfig;
import io.simplesource.kafka.model.ValueWithSequence;
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

import static io.simplesource.example.auction.AppShared.BOOTSTRAP_SERVERS;
import static io.simplesource.example.auction.AppShared.SCHEMA_REGISTRY_URL;
import static io.simplesource.kafka.util.KafkaStreamsUtils.*;
import static java.util.Objects.nonNull;

public final class AccountProjectionStreamApp {
    private static final Logger logger = LoggerFactory.getLogger(AccountProjectionStreamApp.class);

    private static final String ACCOUNT_EVENT_TOPIC_NAME = "auction_avro_account-event";
    private static final String PROJECTION_APP_ID = "account_projection_app";
    private static final String ACCOUNT_TRANSACTIONS_PROJECTION_TOPIC = "auction_account_transactions_projection";

    private AccountAvroMappers accountAvroSerdeFactory =
            new AccountAvroMappers(SCHEMA_REGISTRY_URL, false);

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
        AggregateSerdes<AccountKey, AccountCommand, AccountEvent, Optional<Account>> accountAggregateSerdes =
                accountAvroSerdeFactory.createAggregateSerdes();
        EventStreamSpec<AccountKey, AccountEvent> eventStreamSpec =
                EventStreamSpec.create(ACCOUNT_EVENT_TOPIC_NAME, accountAggregateSerdes);

        final StreamsBuilder builder = new StreamsBuilder();

        KStream<AccountKey, ValueWithSequence<AccountEvent>> accountEventStream =
                builder.stream(eventStreamSpec.topicName(), Consumed.with(eventStreamSpec.keySerde(), eventStreamSpec.valueSerde()));

        getProjectors(eventStreamSpec).forEach(p -> p.setupProjection(accountEventStream));

        return builder.build();
    }

    private List<Projector<AccountKey, AccountEvent>> getProjectors(EventStreamSpec<AccountKey, AccountEvent> eventStreamSpec) {
        ProjectionSpec<AccountTransactionKey, Optional<Reservation>> accountTransactionProjectionSpec =
                ProjectionSpecBuilder.<AccountTransactionKey, Optional<Reservation>>newBuilder(SCHEMA_REGISTRY_URL, false)
                        .withTopicName(ACCOUNT_TRANSACTIONS_PROJECTION_TOPIC)
                        .withKeyMapper(AccountProjectionAvroMappers.ACCOUNT_TRANSACTION_KEY_MAPPER)
                        .withValueMapper(AccountProjectionAvroMappers.ACCOUNT_TRANSACTION_PROJECTION_MAPPER)
                        .build();

        return ImmutableList.of(
                new AccountTransactionProjector(eventStreamSpec, accountTransactionProjectionSpec));
    }

    private static KafkaConfig getKafkaConfig() {
        return new KafkaConfig.Builder()
                .withKafkaApplicationId(PROJECTION_APP_ID)
                .withKafkaBootstrap(BOOTSTRAP_SERVERS)
                .build();
    }
}
