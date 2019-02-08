package io.simplesource.example.auction.account.event;

import io.simplesource.example.auction.account.domain.ReservationId;
import io.simplesource.example.auction.core.Money;
import lombok.Value;

public abstract class AccountEvent {
    private AccountEvent() {}

    public interface AccountTransactionEvent {
        ReservationId getReservationId();
    }

    @Value
    public static final class AccountCreated extends AccountEvent {
        final String username;
        final Money initialFunds;
    }

    @Value
    public static final class AccountUpdated extends AccountEvent {
        final String username;
    }

    @Value
    public static final class FundsAdded extends AccountEvent {
        final Money addedFunds;
    }

    @Value
    public static final class FundsReserved extends AccountEvent implements AccountTransactionEvent {
        final ReservationId reservationId;
        final String description;
        final Money amount;

        @Override
        public ReservationId getReservationId() {
            return reservationId;
        }
    }

    @Value
    public static final class FundsReservationCancelled extends AccountEvent implements AccountTransactionEvent {
        final ReservationId reservationId;

        @Override
        public ReservationId getReservationId() {
            return reservationId;
        }
    }

    @Value
    public static final class ReservationConfirmed extends AccountEvent implements AccountTransactionEvent {
        final ReservationId reservationId;
        final Money amount;

        @Override
        public ReservationId getReservationId() {
            return reservationId;
        }
    }

}
