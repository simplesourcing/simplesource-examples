package io.simplesource.example.auction.account.command;

import io.simplesource.api.CommandError;
import io.simplesource.api.CommandHandler;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Result;
import io.simplesource.dsl.CommandHandlerBuilder;
import io.simplesource.example.auction.account.domain.Account;
import io.simplesource.example.auction.account.domain.AccountKey;
import io.simplesource.example.auction.account.domain.Reservation;
import io.simplesource.example.auction.account.event.AccountEvent;
import io.simplesource.example.auction.core.Money;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.simplesource.data.NonEmptyList.of;

public class AccountCommandHandler {

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
            .map(d -> success(new AccountEvent.AccountUpdated(command.username())))
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

              Optional<CommandError> insufficientFunds = validateReserveFunds(command.funds(), d, Optional.empty());

              List<CommandError> validationErrorReasons = Stream.of(
                      reservationExists,
                      insufficientFunds)
                      .filter(Optional::isPresent)
                      .map(Optional::get).collect(Collectors.toList());

              return NonEmptyList.fromList(validationErrorReasons)
                      .map(AccountCommandHandler::failure)
                      .orElse(success(new AccountEvent.FundsReserved(command.reservationId(), command.description(), command.funds())));
            })
            .orElse(failure("Can not find an account with ID: " + accountId.id()));
  }

  private static CommandHandler<AccountKey, AccountCommand.CancelReservation, AccountEvent, Optional<Account>> doCancelReservation() {
    return (accountId, currentAggregate, command) -> currentAggregate
            .map(d -> {
              if (d.fundReservations().stream().noneMatch(c ->
                      c.reservationId().id().equals(command.reservationId().id()))) {
                return failure("Reservation can not be found for this account");
              }
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
                      Optional.empty() : Optional.of(CommandError.of(CommandError.Reason.InvalidCommand, "Reservation already exists in this account"));

              Optional<CommandError> insufficientFunds = validateReserveFunds(command.finalAmount(), d, existingReservation);

              List<CommandError> validationErrorReasons = Stream.of(
                      reservationMissing,
                      insufficientFunds)
                      .filter(Optional::isPresent)
                      .map(Optional::get).collect(Collectors.toList());

              return NonEmptyList.fromList(validationErrorReasons)
                      .map(AccountCommandHandler::failure)
                      .orElse(success(new AccountEvent.ReservationConfirmed(command.reservationId(), command.finalAmount())));
            })
            .orElse(failure("Can not find an account with ID: " + accountId.id()));
  }

  private static Optional<CommandError> validateReserveFunds(Money funds, Account account, Optional<Reservation> reservation) {
    if ((account.availableFunds().add(reservation.map(res -> res.amount()).orElse(Money.ZERO))).compareTo(funds) < 0)
      return Optional.of(CommandError.of(CommandError.Reason.InvalidCommand, "Insufficient funds available"));

    return Optional.empty();
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
