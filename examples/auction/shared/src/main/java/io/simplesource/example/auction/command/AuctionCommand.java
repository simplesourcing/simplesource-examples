package io.simplesource.example.auction.command;

import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.example.auction.domain.ReservationId;
import lombok.Value;

import java.time.Duration;
import java.time.Instant;

public abstract class AuctionCommand {
    private AuctionCommand() {}

    @Value
    public static final class CreateAuction extends AuctionCommand {
        private final String creator;
        private final String title;
        private final String description;
        private final Money reservePrice;
        private final Duration duration;
    }

    @Value
    public static final class StartAuction extends AuctionCommand {
        private final Instant start;
    }

    @Value
    public static final class PlaceBid extends AuctionCommand {
        private ReservationId reservationId;
        private Instant timestamp;
        private AccountKey bidder;
        private Money amount;
    }

    @Value
    public static final class CompleteAuction extends AuctionCommand {
    }
}
