package io.simplesource.example.auction.event;

import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.example.auction.domain.ReservationId;
import lombok.Value;

import java.time.Duration;
import java.time.Instant;

public abstract class AuctionEvent {
    private AuctionEvent() {}

    @Value
    public static final class AuctionCreated extends AuctionEvent {
        final String creator;
        final String title;
        final String description;
        final Money reservePrice;
        final Duration duration;
    }

    @Value
    public static final class AuctionStarted extends AuctionEvent {
        final Instant started;
    }

    @Value
    public static final class BidPlaced extends AuctionEvent {
        final ReservationId reservationId;
        final Instant timestamp;
        final AccountKey bidder;
        final Money amount;
    }

    @Value
    public static final class AuctionCompleted extends AuctionEvent {
    }
}
