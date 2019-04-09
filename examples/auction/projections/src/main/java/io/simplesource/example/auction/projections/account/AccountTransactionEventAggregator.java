package io.simplesource.example.auction.projections.account;

import io.simplesource.api.Aggregator;
import io.simplesource.dsl.AggregatorBuilder;
import io.simplesource.example.auction.event.AccountEvent;
import io.simplesource.example.auction.domain.Reservation;

import java.util.Optional;

/**
 * Aggregates account transaction events into a Reservation.
 */
public class AccountTransactionEventAggregator {

    static Aggregator<AccountEvent.AccountTransactionEvent, Optional<Reservation>> getAggregator() {
        return AggregatorBuilder.<AccountEvent.AccountTransactionEvent, Optional<Reservation>>newBuilder()
                .onEvent(AccountEvent.FundsReserved.class, onTransactionCreated())
                .onEvent(AccountEvent.ReservationConfirmed.class, onTransactionConfirmed())
                .onEvent(AccountEvent.FundsReservationCancelled.class, onTransactionCancelled())
                .build();
    }

    private static Aggregator<AccountEvent.FundsReserved, Optional<Reservation>> onTransactionCreated() {
        return (r, e) -> Optional.of(new Reservation(e.reservationId(), e.timestamp(), e.auction(), e.amount(), e.description(), Reservation.Status.DRAFT));
    }

    private static Aggregator<AccountEvent.ReservationConfirmed, Optional<Reservation>> onTransactionConfirmed() {
        return (r, e) -> r.map(p -> p.toBuilder().status(Reservation.Status.CONFIRMED).amount(e.amount()).build());
    }

    private static Aggregator<AccountEvent.FundsReservationCancelled, Optional<Reservation>> onTransactionCancelled() {
        return (r, e) -> r.map(p -> p.toBuilder().status(Reservation.Status.CANCELLED).build());
    }
}
