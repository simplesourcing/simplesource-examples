package io.simplesource.example.auction.account.domain;

import io.simplesource.example.auction.core.Money;
import lombok.Value;

public interface AccountCommand {
    @Value
    class CreateAccount implements AccountCommand {
        private final String username;
        private final Money initialFunds;
    }

    @Value
    class UpdateAccount implements AccountCommand {
        private final String username;
    }

    @Value
    class AddFunds implements AccountCommand {
        private final Money funds;
    }

    @Value
    class ReserveFunds implements AccountCommand {
        private final ReservationId reservationId;
        private final Money funds;
        private final String description;
    }

    @Value
    class CancelReservation implements AccountCommand {
        private final ReservationId reservationId;
    }

    @Value
    class ConfirmReservation implements AccountCommand {
        private final ReservationId reservationId;
        private final Money finalAmount;
    }
}
