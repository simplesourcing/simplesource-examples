package io.simplesource.example.auction.server.command;

import io.simplesource.api.CommandError;
import io.simplesource.api.CommandHandler;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Result;
import io.simplesource.dsl.CommandHandlerBuilder;
import io.simplesource.example.auction.command.AccountCommand;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.Account;
import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.example.auction.domain.Reservation;
import io.simplesource.example.auction.event.AccountEvent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.simplesource.data.NonEmptyList.of;

public final class AccountCommandHandler {

    public static CommandHandler<AccountKey, AccountCommand, AccountEvent, Optional<Account>> instance =
            CommandHandlerBuilder.<AccountKey, AccountCommand, AccountEvent, Optional<Account>>newBuilder()
                    .onCommand(AccountCommand.CreateAccount.class, doCreateAccount())
                    .onCommand(AccountCommand.UpdateAccount.class, doUpdateAccount())
                    .onCommand(AccountCommand.AddFunds.class, doAddFunds())
                    .onCommand(AccountCommand.ReserveFunds.class, doReserveFund())
                    .onCommand(AccountCommand.CancelReservation.class, doCancelReservation())
                    .onCommand(AccountCommand.ConfirmReservation.class, doConfirmReservation())
                    .build();

    private static CommandHandler<AccountKey, AccountCommand.CreateAccount, AccountEvent, Optional<Account>> doCreateAccount() {
        return (accountId, currentAggregate, command) -> currentAggregate
                .map(d -> failure("Account already created: " + accountId.id()))
                .orElse(success(new AccountEvent.AccountCreated(command.username(), command.initialFunds())));
    }

    private static CommandHandler<AccountKey, AccountCommand.UpdateAccount, AccountEvent, Optional<Account>> doUpdateAccount() {
        return (accountId, currentAggregate, command) -> currentAggregate
                .map(d -> AccountCommandHandler.success(new AccountEvent.AccountUpdated(command.username())))
                .orElse(failure("Can not find an account with ID: " + accountId.id()));
    }

    private static CommandHandler<AccountKey, AccountCommand.AddFunds, AccountEvent, Optional<Account>> doAddFunds() {
        return (accountId, currentAggregate, command) -> currentAggregate
                .map(d -> success(new AccountEvent.FundsAdded(command.funds())))
                .orElse(failure("Can not find an account with ID: " + accountId.id()));
    }

    private static CommandHandler<AccountKey, AccountCommand.ReserveFunds, AccountEvent, Optional<Account>> doReserveFund() {
        return (accountId, currentAggregate, command) -> currentAggregate
                .map(d -> {
                    Optional<CommandError> reservationExists = d.fundReservations().stream().anyMatch(c ->
                            c.reservationId().id().equals(command.reservationId().id())) ?
                            Optional.of(CommandError.of(CommandError.Reason.InvalidCommand, "Reservation already exists in this account")) :
                            Optional.empty();

                    Reservation reservation = new Reservation(command.reservationId(), command.timestamp(), command.auction(), command.funds(),
                            command.description(), Reservation.Status.DRAFT);
                    Optional<CommandError> insufficientFunds =
                            d.reserve(reservation).availableFunds().compareTo(Money.ZERO) < 0 ?
                                    Optional.of(CommandError.of(CommandError.Reason.InvalidCommand, "Insufficient funds available")) :
                                    Optional.empty();

                    List<CommandError> validationErrorReasons = Stream.of(
                            reservationExists,
                            insufficientFunds)
                            .filter(Optional::isPresent)
                            .map(Optional::get).collect(Collectors.toList());

                    return NonEmptyList.fromList(validationErrorReasons)
                            .map(AccountCommandHandler::failure)
                            .orElseGet(() ->
                                    success(new AccountEvent.FundsReserved(command.reservationId(), command.timestamp(), command.auction(),
                                            command.funds(), command.description()))
                            );
                })
                .orElse(failure("Can not find an account with ID: " + accountId.id()));
    }

    private static CommandHandler<AccountKey, AccountCommand.CancelReservation, AccountEvent, Optional<Account>> doCancelReservation() {
        return (accountId, currentAggregate, command) -> currentAggregate
                .map(d -> {
                    // no need to validate reservation exists as it could have been removed/replaced when the user raised a bid
                    return success(new AccountEvent.FundsReservationCancelled(command.reservationId()));
                })
                .orElse(failure("Can not find an account with ID: " + accountId.id()));
    }

    private static CommandHandler<AccountKey, AccountCommand.ConfirmReservation, AccountEvent, Optional<Account>> doConfirmReservation() {
        return (accountId, currentAggregate, command) -> currentAggregate
                .map(d -> {

                    Optional<Reservation> existingReservation = d.fundReservations().stream().filter(c ->
                            c.reservationId().id().equals(command.reservationId().id())).findAny();

                    Optional<CommandError> reservationMissing = existingReservation.isPresent() ?
                            Optional.empty() : Optional.of(CommandError.of(CommandError.Reason.InvalidCommand, "Reservation can not be found for this account"));

                    Optional<CommandError> insufficientFunds = existingReservation.filter(r -> r.amount().compareTo(command.finalAmount()) < 0)
                            .map(r -> CommandError.of(CommandError.Reason.InvalidCommand, "Insufficient funds available"));

                    List<CommandError> validationErrorReasons = Stream.of(
                            reservationMissing,
                            insufficientFunds)
                            .filter(Optional::isPresent)
                            .map(Optional::get).collect(Collectors.toList());

                    return NonEmptyList.fromList(validationErrorReasons)
                            .map(AccountCommandHandler::failure)
                            .orElseGet(() ->
                                    success(new AccountEvent.ReservationConfirmed(command.reservationId(), command.finalAmount()))
                            );
                })
                .orElse(failure("Can not find an account with ID: " + accountId.id()));
    }

    private static Result<CommandError, NonEmptyList<AccountEvent>> failure(final String message) {
        return Result.failure(CommandError.of(CommandError.Reason.InvalidCommand, message));
    }

    private static Result<CommandError, NonEmptyList<AccountEvent>> failure(final NonEmptyList<CommandError> reasons) {
        return Result.failure(reasons);
    }

    @SafeVarargs
    private static <Event extends AccountEvent> Result<CommandError, NonEmptyList<AccountEvent>> success(final Event event, final Event... events) {
        return Result.success(of(event, events));
    }
}
