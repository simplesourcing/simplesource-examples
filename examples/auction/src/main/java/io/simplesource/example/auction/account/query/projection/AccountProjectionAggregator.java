package io.simplesource.example.auction.account.query.projection;

import io.simplesource.api.Aggregator;
import io.simplesource.dsl.AggregatorBuilder;
import io.simplesource.example.auction.account.event.AccountEvent.AccountTransactionEvent;
import io.simplesource.example.auction.account.event.AccountEvent.FundsReservationCancelled;
import io.simplesource.example.auction.account.event.AccountEvent.FundsReserved;
import io.simplesource.example.auction.account.event.AccountEvent.ReservationConfirmed;
import io.simplesource.example.auction.account.domain.Reservation;

import java.util.Optional;

public final class AccountProjectionAggregator {

    public static Aggregator<AccountTransactionEvent, Optional<Reservation>> accountTransactionEventAggregator() {
        return AggregatorBuilder.<AccountTransactionEvent, Optional<Reservation>>newBuilder()
                .onEvent(FundsReserved.class, onTransactionCreated())
                .onEvent(ReservationConfirmed.class, onTransactionConfirmed())
                .onEvent(FundsReservationCancelled.class, onTransactionCancelled())
                .build();
    }

    private static Aggregator<FundsReserved, Optional<Reservation>> onTransactionCreated() {
        return (r, e) -> Optional.of(new Reservation(e.reservationId(), e.description(), e.amount(), Reservation.Status.DRAFT));
    }

    private static Aggregator<ReservationConfirmed, Optional<Reservation>> onTransactionConfirmed() {
        return (r, e) -> r.map(p -> p.toBuilder().status(Reservation.Status.CONFIRMED).amount(e.amount()).build());
    }

    private static Aggregator<FundsReservationCancelled, Optional<Reservation>> onTransactionCancelled() {
        return (r, e) -> r.map(p -> p.toBuilder().status(Reservation.Status.CANCELLED).build());
    }
}
