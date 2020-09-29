package io.simplesource.example.auction.server;

import io.simplesource.example.auction.avro.AccountAvroMappers;
import io.simplesource.example.auction.avro.AllocationAvroMappers;
import io.simplesource.example.auction.avro.AuctionAvroMappers;
import io.simplesource.example.auction.server.aggregate.AccountAggregate;
import io.simplesource.example.auction.server.aggregate.AllocationAggregate;
import io.simplesource.example.auction.server.aggregate.AuctionAggregate;
import io.simplesource.kafka.dsl.EventSourcedApp;

import java.util.Optional;

import static io.simplesource.example.auction.AppShared.*;

/**
 * This is the main simple sourcing server application that handles client commands to create events and aggregates.
 */
public class StreamsApplication {
    public static void main(final String[] args) {
        runCommandProcessor();
    }

    public static void runCommandProcessor() {
        new EventSourcedApp()
            .withKafkaConfig(builder -> builder
                .withKafkaApplicationId("auction_app")
                .withKafkaBootstrap(BOOTSTRAP_SERVERS)
                .build())
            .withAggregate(AccountAggregate.createSpec(
                    ACCOUNT_AGGREGATE_NAME,
                    new AccountAvroMappers(SCHEMA_REGISTRY_URL, false).createAggregateSerdes(),
                    resourceNamingStrategy(),
                    (k) -> Optional.empty()))
            .withAggregate(AuctionAggregate.createSpec(
                    AUCTION_AGGREGATE_NAME,
                    new AuctionAvroMappers(SCHEMA_REGISTRY_URL, false).createAggregateSerdes(),
                    resourceNamingStrategy(),
                    (k) -> Optional.empty()))
            .withAggregate(AllocationAggregate.createSpec(
                    USERNAME_ALLOCATION_AGGREGATE_NAME,
                    new AllocationAvroMappers(SCHEMA_REGISTRY_URL, false).createAggregateSerdes(),
                    resourceNamingStrategy(),
                    (k) -> Optional.empty()))
            .start();
    }
}


