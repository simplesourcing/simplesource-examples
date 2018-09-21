package io.simplesource.example.auction.account.command;

import io.simplesource.api.*;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Reason;
import io.simplesource.data.Result;
import io.simplesource.dsl.CommandHandlerBuilder;
import io.simplesource.example.auction.account.domain.*;
import io.simplesource.example.auction.core.Money;
import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.api.ResourceNamingStrategy;
import io.simplesource.kafka.dsl.AggregateBuilder;
import io.simplesource.kafka.spec.AggregateSpec;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.simplesource.api.CommandAPI.CommandError.InvalidCommand;
import static io.simplesource.data.NonEmptyList.of;

public final class AccountMappedAggregate {
    static public <S> AggregateSpec<AccountKey, AccountCommand, AccountEvents.AccountEvent, Optional<Account>> createSpec(
            final String name,
            final AggregateSerdes<AccountKey, AccountCommand, AccountEvents.AccountEvent, Optional<Account>> aggregateSerdes,
            final ResourceNamingStrategy resourceNamingStrategy,
            final InitialValue<AccountKey, Optional<Account>> initialValue
    ) {
        return AggregateBuilder.<AccountKey, AccountCommand, AccountEvents.AccountEvent, Optional<Account>>newBuilder()
                .withName(name)
                .withDomainSerializer(aggregateSerdes)
                .withResourceNamingStrategy(resourceNamingStrategy)
                .withInitialValue(initialValue)
                .withAggregator(AccountEvents.getAggregator())
                .withCommandHandler(accountCommandHandlers())
                .build();
    }

    private static CommandHandler<AccountKey, AccountCommand, AccountEvents.AccountEvent, Optional<Account>> accountCommandHandlers() {
        return CommandHandlerBuilder.<AccountKey, AccountCommand, AccountEvents.AccountEvent, Optional<Account>>newBuilder()
                .onCommand(AccountCommand.CreateAccount.class, doCreateAccount())
                .onCommand(AccountCommand.UpdateAccount.class, doUpdateAccount())
                .onCommand(AccountCommand.AddFunds.class, doAddFunds())
                .onCommand(AccountCommand.ReserveFunds.class, doReserveFund())
                .onCommand(AccountCommand.CancelReservation.class, doCancelReservation())
                .onCommand(AccountCommand.ConfirmReservation.class, doConfirmReservation())
                .build();
    }

    private static CommandHandler<AccountKey, AccountCommand.CreateAccount, AccountEvents.AccountEvent, Optional<Account>> doCreateAccount() {
        return CommandHandler.ifSeq(
                (accountId, expectedSeq, currentSeq, currentAggregate, command) -> currentAggregate
                        .map(d -> failure("Account already created: " + accountId.id()))
                        .orElse(success(new AccountEvents.AccountCreated(command.username(),command.initialFunds()))));
    }

    private static CommandHandler<AccountKey, AccountCommand.UpdateAccount, AccountEvents.AccountEvent, Optional<Account>> doUpdateAccount() {
        return CommandHandler.ifSeq(
                (accountId, expectedSeq, currentSeq, currentAggregate, command) -> currentAggregate
                        .map(d -> AccountMappedAggregate.success(new AccountEvents.AccountUpdated(command.username())))
                        .orElse(failure("Can not find an account with ID: " + accountId.id())));
    }

    private static CommandHandler<AccountKey, AccountCommand.AddFunds, AccountEvents.AccountEvent, Optional<Account>> doAddFunds() {
        return CommandHandler.ifSeq(
                (accountId, expectedSeq, currentSeq, currentAggregate, command) -> currentAggregate
                        .map(d -> success(new AccountEvents.FundsAdded(command.funds())))
                        .orElse(failure("Can not find an account with ID: " + accountId.id())));
    }

    private static CommandHandler<AccountKey, AccountCommand.ReserveFunds, AccountEvents.AccountEvent, Optional<Account>> doReserveFund() {
        return CommandHandler.ifSeq(
                (accountId, expectedSeq, currentSeq, currentAggregate, command) -> currentAggregate
                        .map(d -> {
                            Optional<Reason<CommandAPI.CommandError>> reservationExists = d.fundReservations().stream().anyMatch(c ->
                                    c.reservationId().id().equals(command.reservationId().id())) ?
                                    Optional.of(Reason.of(InvalidCommand,"Reservation already exists in this account")) :
                                    Optional.empty();

                            Optional<Reason<CommandAPI.CommandError>> insufficientFunds = validateReserveFunds(command.funds(), d, Optional.empty());

                            List<Reason<CommandAPI.CommandError>> validationErrorReasons = Stream.of(
                                    reservationExists,
                                    insufficientFunds)
                                    .filter(inv -> inv.isPresent())
                                    .map(Optional::get).collect(Collectors.toList());

                            if ( !validationErrorReasons.isEmpty() ) {
                                return failure(NonEmptyList.fromList(validationErrorReasons));
                            }

                            return success(new AccountEvents.FundsReserved(command.reservationId(), command.description(), command.funds()));
                        })
                        .orElse(failure("Can not find an account with ID: " + accountId.id())));
    }

    private static CommandHandler<AccountKey, AccountCommand.CancelReservation, AccountEvents.AccountEvent, Optional<Account>> doCancelReservation() {
        return CommandHandler.ifSeq(
                (accountId, expectedSeq, currentSeq, currentAggregate, command) -> currentAggregate
                        .map(d -> {
                            if (d.fundReservations().stream().noneMatch(c ->
                                    c.reservationId().id().equals(command.reservationId().id()))) {
                                return failure("Reservation can not be found for this account");
                            }
                            return success(new AccountEvents.FundsReservationCancelled(command.reservationId()));
                        })
                        .orElse(failure("Can not find an account with ID: " + accountId.id())));
    }

    private static CommandHandler<AccountKey, AccountCommand.ConfirmReservation, AccountEvents.AccountEvent, Optional<Account>> doConfirmReservation() {
        return CommandHandler.ifSeq(
                (accountId, expectedSeq, currentSeq, currentAggregate, command) -> currentAggregate
                        .map(d -> {

                            Optional<Reservation> existingReservation = d.fundReservations().stream().filter(c ->
                                    c.reservationId().id().equals(command.reservationId().id())).findAny();

                            Optional<Reason<CommandAPI.CommandError>> reservationMissing = existingReservation.isPresent() ?
                                    Optional.empty() : Optional.of(Reason.of(InvalidCommand,"Reservation already exists in this account"));

                            Optional<Reason<CommandAPI.CommandError>> insufficientFunds = validateReserveFunds(command.finalAmount(), d, existingReservation);

                            List<Reason<CommandAPI.CommandError>> validationErrorReasons = Stream.of(
                                    reservationMissing,
                                    insufficientFunds)
                                    .filter(inv -> inv.isPresent())
                                    .map(Optional::get).collect(Collectors.toList());

                            if ( !validationErrorReasons.isEmpty() ) {
                                return failure(NonEmptyList.fromList(validationErrorReasons));
                            }

                            return success(new AccountEvents.ReservationConfirmed(command.reservationId(), command.finalAmount()));
                        })
                        .orElse(failure("Can not find an account with ID: " + accountId.id())));
    }

    private static Optional<Reason<CommandAPI.CommandError>> validateReserveFunds(Money funds, Account account, Optional<Reservation> reservation) {
        if ((account.availableFunds().add(reservation.map(res -> res.amount()).orElse(Money.ZERO))).compareTo(funds) < 0)
            return Optional.of(Reason.of(InvalidCommand, "Insufficient funds available"));

        return Optional.empty();
    }

    private static Result<CommandAPI.CommandError, NonEmptyList<AccountEvents.AccountEvent>> failure(final String message) {
        return Result.failure(InvalidCommand, message);
    }

    private static Result<CommandAPI.CommandError, NonEmptyList<AccountEvents.AccountEvent>> failure(final NonEmptyList<Reason<CommandAPI.CommandError>> reasons) {
        return Result.failure(reasons);
    }

    @SafeVarargs
    private static <Event extends AccountEvents.AccountEvent> Result<CommandAPI.CommandError, NonEmptyList<AccountEvents.AccountEvent>> success(final Event event, final Event... events) {
        return Result.success(of(event, events));
    }
}
