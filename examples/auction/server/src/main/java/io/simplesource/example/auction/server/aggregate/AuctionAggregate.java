package io.simplesource.example.auction.server.aggregate;

import io.simplesource.api.InitialValue;
import io.simplesource.example.auction.command.AuctionCommand;
import io.simplesource.example.auction.domain.Auction;
import io.simplesource.example.auction.domain.AuctionKey;
import io.simplesource.example.auction.event.AuctionEvent;
import io.simplesource.example.auction.event.AuctionEventHandler;
import io.simplesource.example.auction.server.command.AuctionCommandHandler;
import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.api.ResourceNamingStrategy;
import io.simplesource.kafka.dsl.AggregateBuilder;
import io.simplesource.kafka.dsl.InvalidSequenceStrategy;
import io.simplesource.kafka.spec.AggregateSpec;

import java.util.Optional;

public class AuctionAggregate {

    public static AggregateSpec<AuctionKey, AuctionCommand, AuctionEvent, Optional<Auction>> createSpec(
            final String name,
            final AggregateSerdes<AuctionKey, AuctionCommand, AuctionEvent, Optional<Auction>> aggregateSerdes,
            final ResourceNamingStrategy resourceNamingStrategy,
            final InitialValue<AuctionKey, Optional<Auction>> initialValue
    ) {
        return AggregateBuilder.<AuctionKey, AuctionCommand, AuctionEvent, Optional<Auction>>newBuilder()
                .withName(name)
                .withSerdes(aggregateSerdes)
                .withInvalidSequenceStrategy(InvalidSequenceStrategy.Strict)
                .withResourceNamingStrategy(resourceNamingStrategy)
                .withInitialValue(initialValue)
                .withAggregator(AuctionEventHandler.instance)
                .withCommandHandler(AuctionCommandHandler.instance)
                .withDefaultTopicSpec(6, 1, 1)
                .build();
    }
}
