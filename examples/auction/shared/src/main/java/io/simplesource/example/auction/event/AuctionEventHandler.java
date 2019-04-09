package io.simplesource.example.auction.event;

import io.simplesource.api.Aggregator;
import io.simplesource.dsl.AggregatorBuilder;
import io.simplesource.example.auction.auction.wire.AuctionStatus;
import io.simplesource.example.auction.domain.Auction;
import io.simplesource.example.auction.domain.Bid;

import java.util.Collections;
import java.util.Optional;

public final class AuctionEventHandler {
    private AuctionEventHandler() {
    }

    public static Aggregator<AuctionEvent, Optional<Auction>> instance =
            AggregatorBuilder.<AuctionEvent, Optional<Auction>>newBuilder()
                    .onEvent(AuctionEvent.AuctionCreated.class, handleAuctionCreated())
                    .onEvent(AuctionEvent.AuctionStarted.class, handleAuctionStarted())
                    .onEvent(AuctionEvent.BidPlaced.class, handleBidPlaced())
                    .onEvent(AuctionEvent.AuctionCompleted.class, handleAuctionCompleted())
                    .build();

    private static Aggregator<AuctionEvent.AuctionCreated, Optional<Auction>> handleAuctionCreated() {
        return (currentAggregate, event) ->
                Optional.of(new Auction(
                        event.creator(),
                        event.title(),
                        event.description(),
                        event.reservePrice(),
                        event.reservePrice(),
                        event.duration(),
                        AuctionStatus.CREATED,
                        null,
                        null,
                        Collections.emptyList()
                ));
    }

    private static Aggregator<AuctionEvent.AuctionStarted, Optional<Auction>> handleAuctionStarted() {
        return (currentAggregate, event) ->
                currentAggregate.map(a ->
                        a.toBuilder()
                                .status(AuctionStatus.STARTED)
                                .start(event.started())
                                .build()
                );
    }

    private static Aggregator<AuctionEvent.BidPlaced, Optional<Auction>> handleBidPlaced() {
        return (currentAggregate, event) ->
                currentAggregate.map(a -> {
                    return a.addBid(new Bid(event.reservationId(), event.timestamp(), event.bidder(), event.amount()));
                });
    }

    private static Aggregator<AuctionEvent.AuctionCompleted, Optional<Auction>> handleAuctionCompleted() {
        return (currentAggregate, event) ->
                currentAggregate.map(a ->
                        a.winningBid().map(b ->
                                a.toBuilder()
                                        .winner(b.bidder())
                                        .status(AuctionStatus.COMPLETED)
                                        .build()
                        ).orElse(
                                a.toBuilder()
                                        .status(AuctionStatus.COMPLETED).build()
                        )
                );
    }
}
