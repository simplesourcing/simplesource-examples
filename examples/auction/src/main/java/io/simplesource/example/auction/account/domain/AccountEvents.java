package io.simplesource.example.auction.account.domain;

import io.simplesource.api.Aggregator;
import io.simplesource.dsl.AggregatorBuilder;
import io.simplesource.example.auction.core.Money;
import lombok.Value;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;

public final class AccountEvents {

    public interface AccountEvent {
    }

    public interface AccountTransactionEvent {
        ReservationId getReservationId();
    }

    @Value
    public static final class AccountCreated implements AccountEvent {
        final String username;
        final Money initialFunds;
    }

    @Value
    public static final class AccountUpdated implements AccountEvent {
        final String username;
    }

    @Value
    public static final class FundsAdded implements AccountEvent {
        final Money addedFunds;
    }

    @Value
    public static final class FundsReserved implements AccountEvent, AccountTransactionEvent {
        final ReservationId reservationId;
        final String description;
        final Money amount;

        @Override
        public ReservationId getReservationId() {
            return reservationId;
        }
    }

    @Value
    public static final class FundsReservationCancelled implements AccountEvent, AccountTransactionEvent {
        final ReservationId reservationId;

        @Override
        public ReservationId getReservationId() {
            return reservationId;
        }
    }

    @Value
    public static final class ReservationConfirmed implements AccountEvent, AccountTransactionEvent {
        final ReservationId reservationId;
        final Money amount;

        @Override
        public ReservationId getReservationId() {
            return reservationId;
        }
    }

    private static Aggregator<AccountCreated, Optional<Account>> handleAccountCreated() {
        return (currentAggregate, event) ->
                Optional.of(new Account(event.username(), event.initialFunds(), emptyList()));
    }

    private static Aggregator<AccountUpdated, Optional<Account>> handleAccountUpdated() {
        return (currentAggregate, event) -> currentAggregate.map(r -> r.toBuilder().username(event.username()).build());
    }

    private static Aggregator<FundsAdded, Optional<Account>> handleFundsAdded() {
        return (currentAggregate, event) -> currentAggregate.map(r -> r.addFunds(event.addedFunds()));
    }

    private static Aggregator<FundsReserved, Optional<Account>> handleFundsReserved() {
        return (currentAggregate, event) -> currentAggregate.map(d -> addFundReservation(event, d));
    }

    private static Aggregator<FundsReservationCancelled, Optional<Account>> handleCancelFundsReservation() {
        return (currentAggregate, event) -> currentAggregate.map(d -> cancelFundReservation(event, d));
    }

    private static Aggregator<ReservationConfirmed, Optional<Account>> handleConfirmFundsReservation() {
        return (currentAggregate, event) -> currentAggregate.map(d -> confirmFundReservation(event, d));
    }

    private static Account addFundReservation(FundsReserved event, Account currentAggregate) {
        currentAggregate.fundReservations().add(new Reservation(event.reservationId(), event.description(), event.amount(),
                Reservation.Status.DRAFT));
        return currentAggregate;
    }

    private static Account cancelFundReservation(FundsReservationCancelled event, Account account) {
        List<Reservation> draftFundReservations = removeDraftReservationIfExists(account.fundReservations(), event.reservationId());
        return account.toBuilder().fundReservations(draftFundReservations).build();
    }

    private static Account confirmFundReservation(ReservationConfirmed event, Account account) {
        List<Reservation> reservations = account.fundReservations();
        List<Reservation> onlyDraftReservations = removeDraftReservationIfExists(reservations, event.reservationId());

        if ( reservations.size() == onlyDraftReservations.size() ) {
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

    public static Aggregator<AccountEvent, Optional<Account>> getAggregator() {
        return AggregatorBuilder.<AccountEvent, Optional<Account>>newBuilder()
                .onEvent(AccountCreated.class, handleAccountCreated())
                .onEvent(AccountUpdated.class, handleAccountUpdated())
                .onEvent(FundsAdded.class, handleFundsAdded())
                .onEvent(FundsReserved.class, handleFundsReserved())
                .onEvent(FundsReservationCancelled.class, handleCancelFundsReservation())
                .onEvent(ReservationConfirmed.class, handleConfirmFundsReservation())
                .build();
    }
}
