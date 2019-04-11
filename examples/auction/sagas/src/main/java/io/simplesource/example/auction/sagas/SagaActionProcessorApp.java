package io.simplesource.example.auction.sagas;

import io.simplesource.data.Result;
import io.simplesource.data.Sequence;
import io.simplesource.example.auction.AppShared;
import io.simplesource.example.auction.account.wire.AccountSagaCommand;
import io.simplesource.example.auction.allocation.wire.AllocationSagaCommand;
import io.simplesource.example.auction.auction.wire.AuctionSagaCommand;
import io.simplesource.example.auction.avro.AccountAvroMappers;
import io.simplesource.example.auction.avro.AllocationAvroMappers;
import io.simplesource.example.auction.avro.AuctionAvroMappers;
import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.example.auction.domain.AllocationKey;
import io.simplesource.example.auction.domain.AuctionKey;
import io.simplesource.kafka.serialization.avro.AvroCommandSerdes;
import io.simplesource.kafka.serialization.avro.AvroSpecificGenericMapper;
import io.simplesource.kafka.spec.WindowSpec;
import io.simplesource.saga.action.ActionApp;
import io.simplesource.saga.action.eventsourcing.EventSourcingBuilder;
import io.simplesource.saga.action.eventsourcing.EventSourcingSpec;
import io.simplesource.saga.model.specs.ActionSpec;
import io.simplesource.saga.model.specs.SagaSpec;
import io.simplesource.saga.saga.SagaApp;
import io.simplesource.saga.serialization.avro.AvroSerdes;
import io.simplesource.saga.shared.streams.StreamAppConfig;
import io.simplesource.saga.shared.topics.TopicNamer;
import org.apache.avro.generic.GenericRecord;

import static io.simplesource.example.auction.AppShared.*;

import java.time.Duration;
import java.util.Optional;

/**
 * App to convert saga actions to auction commands.
 */
public class SagaActionProcessorApp {
    public static void main(String[] args) {
        runActionProcessors();
    }

    public static void runActionProcessors() {
        AccountAvroMappers accountAvroMappers = new AccountAvroMappers(SCHEMA_REGISTRY_URL, false);
        accountAvroMappers.registerConversions();
        EventSourcingSpec<GenericRecord, AccountSagaCommand, AccountKey, GenericRecord> accountCommands = EventSourcingSpec.<GenericRecord, AccountSagaCommand, AccountKey, GenericRecord>of(
                ACCOUNT_AGGREGATE_NAME,
                ACCOUNT_AGGREGATE_NAME,
                r -> Result.success(AvroSpecificGenericMapper.<AccountSagaCommand>specificDomainMapper().fromGeneric(r)),
                r -> (GenericRecord) r.getCommand(),
                r -> AccountKey.of(r.getAccountKey()),
                c -> Sequence.position(c.getSequence()),
                (k, c) -> Optional.empty(),
                new AvroCommandSerdes(accountAvroMappers.buildKeyMapper(),
                        AvroSpecificGenericMapper.specificDomainMapper(), SCHEMA_REGISTRY_URL, false),
                Duration.ofMillis(30000L)
        );

        AuctionAvroMappers auctionAvroMappers = new AuctionAvroMappers(SCHEMA_REGISTRY_URL, false);
        auctionAvroMappers.registerConversions();
        EventSourcingSpec<GenericRecord, AuctionSagaCommand, AuctionKey, GenericRecord> auctionCommands = EventSourcingSpec.<GenericRecord, AuctionSagaCommand, AuctionKey, GenericRecord>of(
                AUCTION_AGGREGATE_NAME,
                AUCTION_AGGREGATE_NAME,
                r -> Result.success(AvroSpecificGenericMapper.<AuctionSagaCommand>specificDomainMapper().fromGeneric(r)),
                r -> (GenericRecord) r.getCommand(),
                r -> AuctionKey.of(r.getAuctionKey()),
                // TODO What to do in lieu of extracting the sequence from the command?
                c -> Sequence.position(c.getSequence()),
                (k, c) -> Optional.empty(),
                new AvroCommandSerdes(auctionAvroMappers.buildKeyMapper(),
                        AvroSpecificGenericMapper.specificDomainMapper(), SCHEMA_REGISTRY_URL, false),
                Duration.ofMillis(30000L)
        );


        AllocationAvroMappers allocationAvroMappers = new AllocationAvroMappers(SCHEMA_REGISTRY_URL, false);
        allocationAvroMappers.registerConversions();
        EventSourcingSpec<GenericRecord, AllocationSagaCommand, AllocationKey, GenericRecord> allocationCommands = EventSourcingSpec.<GenericRecord, AllocationSagaCommand, AllocationKey, GenericRecord>of(
                USERNAME_ALLOCATION_AGGREGATE_NAME,
                USERNAME_ALLOCATION_AGGREGATE_NAME,
                r -> Result.success(AvroSpecificGenericMapper.<AllocationSagaCommand>specificDomainMapper().fromGeneric(r)),
                r -> (GenericRecord) r.getCommand(),
                r -> AllocationKey.of(r.getAllocationId()),
                // TODO What to do in lieu of extracting the sequence from the command?
                c -> Sequence.first(),
                (k, c) -> Optional.empty(),
                new AvroCommandSerdes(allocationAvroMappers.buildKeyMapper(),
                        AvroSpecificGenericMapper.specificDomainMapper(), SCHEMA_REGISTRY_URL, false),
                Duration.ofMillis(30000L)
        );

        ActionApp<GenericRecord> actionApp = ActionApp.of(AvroSerdes.Generic.actionSerdes(AppShared.SCHEMA_REGISTRY_URL, false))
            .withActionProcessor(EventSourcingBuilder.apply(
                accountCommands,
                topicBuilder -> topicBuilder.withTopicNamer(TopicNamer.forPrefix(ACTION_TOPIC_PREFIX, ACCOUNT_AGGREGATE_NAME)).withDefaultConfig(6, 1, 7),
                topicBuilder -> topicBuilder.withTopicNamer(TopicNamer.forPrefix(COMMAND_TOPIC_PREFIX, ACCOUNT_AGGREGATE_NAME)).withDefaultConfig(6, 1, 7)))
            .withActionProcessor(EventSourcingBuilder.apply(
                auctionCommands,
                topicBuilder -> topicBuilder.withTopicNamer(TopicNamer.forPrefix(ACTION_TOPIC_PREFIX, AUCTION_AGGREGATE_NAME)).withDefaultConfig(6, 1, 7),
                topicBuilder -> topicBuilder.withTopicNamer(TopicNamer.forPrefix(COMMAND_TOPIC_PREFIX, AUCTION_AGGREGATE_NAME)).withDefaultConfig(6, 1, 7)))
            .withActionProcessor(EventSourcingBuilder.apply(
                allocationCommands,
                topicBuilder -> topicBuilder.withTopicNamer(TopicNamer.forPrefix(ACTION_TOPIC_PREFIX, USERNAME_ALLOCATION_AGGREGATE_NAME)).withDefaultConfig(6, 1, 7),
                topicBuilder -> topicBuilder.withTopicNamer(TopicNamer.forPrefix(COMMAND_TOPIC_PREFIX, USERNAME_ALLOCATION_AGGREGATE_NAME)).withDefaultConfig(6, 1, 7)));

        actionApp.run(StreamAppConfig.of("sourcing-action-processor-1", BOOTSTRAP_SERVERS));
    }
}
