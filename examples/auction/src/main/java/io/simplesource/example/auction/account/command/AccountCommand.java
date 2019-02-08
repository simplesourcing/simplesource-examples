package io.simplesource.example.auction.account.command;

import io.simplesource.example.auction.account.domain.ReservationId;
import io.simplesource.example.auction.core.Money;
import lombok.Value;

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
