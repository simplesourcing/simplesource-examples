package io.simplesource.example.auction.command;

import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.AuctionKey;
import io.simplesource.example.auction.domain.ReservationId;
import lombok.Value;

import java.time.Instant;

public abstract class AccountCommand {
    private AccountCommand() {}

    @Value
    public static final class CreateAccount extends AccountCommand {
        private final String username;
        private final Money initialFunds;
    }

    @Value
    public static final class UpdateAccount extends AccountCommand {
        private final String username;
    }

    @Value
    public static final class AddFunds extends AccountCommand {
        private final Money funds;
    }

    @Value
    public static final class ReserveFunds extends AccountCommand {
        private final ReservationId reservationId;
        private final Instant timestamp;
        private final AuctionKey auction;
        private final Money funds;
        private final String description;
    }

    @Value
    public static final class CancelReservation extends AccountCommand {
        private final ReservationId reservationId;
    }

    @Value
    public static class ConfirmReservation extends AccountCommand {
        private final ReservationId reservationId;
        private final Money finalAmount;
    }
}
