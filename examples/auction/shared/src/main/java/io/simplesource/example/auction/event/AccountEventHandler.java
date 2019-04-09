package io.simplesource.example.auction.event;

import io.simplesource.api.Aggregator;
import io.simplesource.dsl.AggregatorBuilder;
import io.simplesource.example.auction.domain.Account;
import io.simplesource.example.auction.domain.Reservation;
import io.simplesource.example.auction.domain.ReservationId;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;

public final class AccountEventHandler {
    private AccountEventHandler() {
    }

    public static Aggregator<AccountEvent, Optional<Account>> instance =
            AggregatorBuilder.<AccountEvent, Optional<Account>>newBuilder()
                    .onEvent(AccountEvent.AccountCreated.class, handleAccountCreated())
                    .onEvent(AccountEvent.AccountUpdated.class, handleAccountUpdated())
                    .onEvent(AccountEvent.FundsAdded.class, handleFundsAdded())
                    .onEvent(AccountEvent.FundsReserved.class, handleFundsReserved())
                    .onEvent(AccountEvent.FundsReservationCancelled.class, handleCancelFundsReservation())
                    .onEvent(AccountEvent.ReservationConfirmed.class, handleConfirmFundsReservation())
                    .build();

    private static Aggregator<AccountEvent.AccountCreated, Optional<Account>> handleAccountCreated() {
        return (currentAggregate, event) ->
                Optional.of(new Account(event.username(), event.initialFunds(), emptyList()));
    }

    private static Aggregator<AccountEvent.AccountUpdated, Optional<Account>> handleAccountUpdated() {
        return (currentAggregate, event) -> currentAggregate.map(r -> r.toBuilder().username(event.username()).build());
    }

    private static Aggregator<AccountEvent.FundsAdded, Optional<Account>> handleFundsAdded() {
        return (currentAggregate, event) -> currentAggregate.map(r -> r.addFunds(event.addedFunds()));
    }

    private static Aggregator<AccountEvent.FundsReserved, Optional<Account>> handleFundsReserved() {
        return (currentAggregate, event) -> currentAggregate.map(d -> reserveFunds(event, d));
    }

    private static Aggregator<AccountEvent.FundsReservationCancelled, Optional<Account>> handleCancelFundsReservation() {
        return (currentAggregate, event) -> currentAggregate.map(d -> cancelFundReservation(event, d));
    }

    private static Aggregator<AccountEvent.ReservationConfirmed, Optional<Account>> handleConfirmFundsReservation() {
        return (currentAggregate, event) -> currentAggregate.map(d -> confirmFundReservation(event, d));
    }

    private static Account reserveFunds(AccountEvent.FundsReserved event, Account currentAggregate) {
        return currentAggregate.reserve(new Reservation(event.reservationId(), event.timestamp(), event.auction(), event.amount(),
                event.description(), Reservation.Status.DRAFT));
    }

    private static Account cancelFundReservation(AccountEvent.FundsReservationCancelled event, Account account) {
        List<Reservation> draftFundReservations = removeDraftReservationIfExists(account.fundReservations(), event.reservationId());
        return account.toBuilder().fundReservations(draftFundReservations).build();
    }

    private static Account confirmFundReservation(AccountEvent.ReservationConfirmed event, Account account) {
        List<Reservation> reservations = account.fundReservations();
        List<Reservation> onlyDraftReservations = removeDraftReservationIfExists(reservations, event.reservationId());

        if (reservations.size() == onlyDraftReservations.size()) {
            throw new RuntimeException("Failed to confirm reservation, reservation can not be found in this account");
        }

        return account.toBuilder()
                .fundReservations(onlyDraftReservations)
                .funds(account.funds().subtract(event.amount()))
                .build();
    }

    private static List<Reservation> removeDraftReservationIfExists(List<Reservation> reservations, ReservationId reservationId) {
        List<Reservation> draftReservations = newArrayList(reservations);
        draftReservations.removeIf(r -> r.reservationId().equals(reservationId));
        return draftReservations;
    }
}
