package io.simplesource.example.auction.server.command;

import io.simplesource.api.CommandError;
import io.simplesource.api.CommandHandler;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Result;
import io.simplesource.example.auction.command.AccountCommand;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.*;
import io.simplesource.example.auction.event.AccountEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountCommandHandlerTest {
    private CommandHandler<AccountKey, AccountCommand, AccountEvent, Optional<Account>> handler =
            AccountCommandHandler.instance;

    private AccountKey key = new AccountKey(UUID.randomUUID());

    @Test
    public void createAccountSuccess() {
        Result<CommandError, NonEmptyList<AccountEvent>> result =
                handler.interpretCommand(key, Optional.empty(), new AccountCommand.CreateAccount(
                        "username", Money.valueOf("1")));
        assertThat(result).isEqualTo(Result.success(NonEmptyList.of(new AccountEvent.AccountCreated(
                "username", Money.valueOf("1")))));
    }

    @Test
    public void createAccountFailsIfAlreadyCreated() {
        Account account = new Account("username", Money.valueOf("1"), Collections.emptyList());
        Result<CommandError, NonEmptyList<AccountEvent>> result =
                handler.interpretCommand(key, Optional.of(account), new AccountCommand.CreateAccount(
                        "username", Money.valueOf("1")));
        assertThat(result).isEqualTo(Result.failure(NonEmptyList.of(CommandError.of(CommandError.Reason.InvalidCommand,
                "Account already created: " + key.asString()))));
    }

    @Test
    public void addFundsSuccess() {
        Account account = new Account("username", Money.valueOf("1"), Collections.emptyList());
        Result<CommandError, NonEmptyList<AccountEvent>> result =
                handler.interpretCommand(key, Optional.of(account), new AccountCommand.AddFunds(
                        Money.valueOf("3")));
        assertThat(result).isEqualTo(Result.success(NonEmptyList.of(new AccountEvent.FundsAdded(
                Money.valueOf("3")))));
    }

    @Test
    public void reserveFundsSuccess() {
        ReservationId reservationId = new ReservationId(UUID.randomUUID());
        AuctionKey auction = new AuctionKey(UUID.randomUUID());
        Account account = new Account("username", Money.valueOf("3"), Collections.emptyList());
        Instant timestamp = Instant.now();
        Result<CommandError, NonEmptyList<AccountEvent>> result =
                handler.interpretCommand(key, Optional.of(account), new AccountCommand.ReserveFunds(
                        reservationId, timestamp, auction, Money.valueOf("3"), "desc"));
        assertThat(result).isEqualTo(Result.success(NonEmptyList.of(new AccountEvent.FundsReserved(
                reservationId, timestamp, auction, Money.valueOf("3"), "desc"))));
    }

    @Test
    public void reserveFundsFailsIfInsufficientFunds() {
        ReservationId reservationId = new ReservationId(UUID.randomUUID());
        AuctionKey auction = new AuctionKey(UUID.randomUUID());
        Account account = new Account("username", Money.valueOf("3"), Collections.emptyList());
        Result<CommandError, NonEmptyList<AccountEvent>> result =
                handler.interpretCommand(key, Optional.of(account), new AccountCommand.ReserveFunds(
                        reservationId, Instant.now(), auction, Money.valueOf("5"), "desc"));
        assertThat(result).isEqualTo(Result.failure(NonEmptyList.of(CommandError.of(CommandError.Reason.InvalidCommand,
                "Insufficient funds available"))));
    }

    @Test
    public void reserveFundsFailsIfReservationExists() {
        ReservationId reservationId = new ReservationId(UUID.randomUUID());
        AuctionKey auction = new AuctionKey(UUID.randomUUID());
        Instant timestamp = Instant.now();
        Account account = new Account("username", Money.valueOf("3"), Collections.singletonList(
                new Reservation(reservationId, timestamp, auction, Money.valueOf("1"), "desc", Reservation.Status.DRAFT)));
        Result<CommandError, NonEmptyList<AccountEvent>> result =
                handler.interpretCommand(key, Optional.of(account), new AccountCommand.ReserveFunds(
                        reservationId, timestamp, auction, Money.valueOf("1"), "desc"));
        assertThat(result).isEqualTo(Result.failure(NonEmptyList.of(CommandError.of(CommandError.Reason.InvalidCommand,
                "Reservation already exists in this account"))));
    }

    @Test
    public void cancelReservationSuccess() {
        ReservationId reservationId = new ReservationId(UUID.randomUUID());
        AuctionKey auction = new AuctionKey(UUID.randomUUID());
        Account account = new Account("username", Money.valueOf("3"), Collections.singletonList(
                new Reservation(reservationId, Instant.now(), auction, Money.valueOf("1"), "desc", Reservation.Status.DRAFT)));
        Result<CommandError, NonEmptyList<AccountEvent>> result =
                handler.interpretCommand(key, Optional.of(account), new AccountCommand.CancelReservation(
                        reservationId));
        assertThat(result).isEqualTo(Result.success(NonEmptyList.of(new AccountEvent.FundsReservationCancelled(
                reservationId))));
    }

    @Test
    public void cancelReservationSucceedsEvenIfNotInList() {
        ReservationId reservationId = new ReservationId(UUID.randomUUID());
        Account account = new Account("username", Money.valueOf("3"), Collections.emptyList());
        Result<CommandError, NonEmptyList<AccountEvent>> result =
                handler.interpretCommand(key, Optional.of(account), new AccountCommand.CancelReservation(
                        reservationId));
        assertThat(result).isEqualTo(Result.success(NonEmptyList.of(new AccountEvent.FundsReservationCancelled(
                reservationId))));
    }

    @Test
    public void confirmReservationSuccess() {
        ReservationId reservationId = new ReservationId(UUID.randomUUID());
        AuctionKey auction = new AuctionKey(UUID.randomUUID());
        Account account = new Account("username", Money.valueOf("3"), Collections.singletonList(
                new Reservation(reservationId, Instant.now(), auction, Money.valueOf("1"), "desc", Reservation.Status.DRAFT)));
        Result<CommandError, NonEmptyList<AccountEvent>> result =
                handler.interpretCommand(key, Optional.of(account), new AccountCommand.ConfirmReservation(
                        reservationId, Money.valueOf("1")));
        assertThat(result).isEqualTo(Result.success(NonEmptyList.of(new AccountEvent.ReservationConfirmed(
                reservationId, Money.valueOf("1")))));
    }

    @Test
    public void confirmReservationFailsIfReservationDoesNotExist() {
        ReservationId reservationId = new ReservationId(UUID.randomUUID());
        Account account = new Account("username", Money.valueOf("3"), Collections.emptyList());
        Result<CommandError, NonEmptyList<AccountEvent>> result =
                handler.interpretCommand(key, Optional.of(account), new AccountCommand.ConfirmReservation(
                        reservationId, Money.valueOf("1")));
        assertThat(result).isEqualTo(Result.failure(NonEmptyList.of(CommandError.of(CommandError.Reason.InvalidCommand,
                "Reservation can not be found for this account"))));
    }
}
