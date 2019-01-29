package io.simplesource.example.auction.auction.impl;

import io.simplesource.api.CommandAPI;
import io.simplesource.data.FutureResult;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Sequence;
import io.simplesource.example.auction.account.domain.*;
import io.simplesource.example.auction.account.query.repository.AccountRepository;
import io.simplesource.example.auction.account.query.repository.AccountTransactionRepository;
import io.simplesource.example.auction.account.query.views.AccountTransactionView;
import io.simplesource.example.auction.account.query.views.AccountTransactionViewKey;
import io.simplesource.example.auction.account.query.views.AccountView;
import io.simplesource.example.auction.account.service.AccountWriteService;
import io.simplesource.example.auction.account.service.AccountWriteServiceImpl;
import io.simplesource.example.auction.core.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountWriteServiceImplTest {

    @Mock
    private CommandAPI<AccountKey, AccountCommand> commandApi;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountTransactionRepository accountTransactionRepository;

    private Account account;
    private AccountKey key = new AccountKey(UUID.randomUUID());
    private ReservationId reservationId = new ReservationId(UUID.randomUUID());

    private AccountWriteService accountWriteService;

    @BeforeEach
    void setUp() {
        accountWriteService = new AccountWriteServiceImpl(commandApi, accountRepository, accountTransactionRepository);
        account = new Account("Bob", Money.valueOf("1000"), Collections.emptyList());
    }

    @Test
    void createShouldPublishAndQueryCommand() {
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.empty());
        when(commandApi.publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class)))
                .thenReturn(FutureResult.of(Sequence.first()));

        FutureResult<AccountError, Sequence> result = accountWriteService.createAccount(key, account);
        assertThat(result.future().join().isSuccess()).isTrue();
        verify(commandApi).publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class));
    }

    @Test
    void createShouldReturnErrorWhenAccountWithSameKeyExists() {
        AccountView accountView = new AccountView();
        accountView.setId(key.id().toString());
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.of(accountView));

        FutureResult<AccountError, Sequence> result = accountWriteService.createAccount(key, account);
        assertThat(result.future().join().isFailure()).isTrue();
        assertThat(result.future().join().failureReasons()).contains(
                NonEmptyList.of(AccountError.of(AccountError.Reason.AccountIdAlreadyExist, String.format("Account ID %s already exist", key.asString()))));
        verifyZeroInteractions(commandApi);
    }

    @Test
    void createShouldReturnErrorWhenFundsNegative() {
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.empty());

        Account account = new Account("Bob", Money.valueOf("-1000"), Collections.emptyList());
        FutureResult<AccountError, Sequence> result = accountWriteService.createAccount(key, account);
        assertThat(result.future().join().isFailure()).isTrue();
        assertThat(result.future().join().failureReasons()).contains(
                NonEmptyList.of(AccountError.of(AccountError.Reason.InvalidData, "Initial funds can not be negative")));
        verifyZeroInteractions(commandApi);
    }

    @Test
    void updateShouldPublishAndQueryCommand() {
        AccountView accountView = new AccountView();
        accountView.setId(key.id().toString());
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.of(accountView));
        when(commandApi.publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class)))
                .thenReturn(FutureResult.of(Sequence.first()));

        FutureResult<AccountError, Sequence> result = accountWriteService.updateAccount(key, "Alice");
        assertThat(result.future().join().isSuccess()).isTrue();
        verify(commandApi).publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class));
    }

    @Test
    void addFundsShouldPublishAndQueryCommand() {
        AccountView accountView = new AccountView();
        accountView.setId(key.id().toString());
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.of(accountView));
        when(commandApi.publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class)))
                .thenReturn(FutureResult.of(Sequence.first()));

        FutureResult<AccountError, Sequence> result = accountWriteService.addFunds(key, Money.valueOf("10"));
        assertThat(result.future().join().isSuccess()).isTrue();
        verify(commandApi).publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class));
    }

    @Test
    void addFundsShouldReturnErrorWhenFundsNegative() {
        AccountView accountView = new AccountView();
        accountView.setId(key.id().toString());
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.of(accountView));
        when(commandApi.publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class)))
                .thenReturn(FutureResult.of(Sequence.first()));

        FutureResult<AccountError, Sequence> result = accountWriteService.addFunds(key, Money.valueOf("-10"));
        assertThat(result.future().join().isFailure()).isTrue();
        assertThat(result.future().join().failureReasons()).contains(
                NonEmptyList.of(AccountError.of(AccountError.Reason.InvalidData, "Cannot add a negative amount")));
    }

    @Test
    void reserveFundsShouldPublishAndQueryCommand() {
        AccountView accountView = new AccountView();
        accountView.setId(key.id().toString());
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.of(accountView));
        when(commandApi.publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class)))
                .thenReturn(FutureResult.of(Sequence.first()));

        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .description("Reservation")
                .amount(Money.valueOf("10"))
                .build();
        FutureResult<AccountError, Sequence> result = accountWriteService.reserveFunds(key, reservationId, reservation);
        assertThat(result.future().join().isSuccess()).isTrue();
        verify(commandApi).publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class));
    }

    @Test
    void reserveFundsShouldReturnErrorWhenFundsNegative() {
        AccountView accountView = new AccountView();
        accountView.setId(key.id().toString());
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.of(accountView));

        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .description("Reservation")
                .amount(Money.valueOf("-10"))
                .build();
        FutureResult<AccountError, Sequence> result = accountWriteService.reserveFunds(key, reservationId, reservation);
        assertThat(result.future().join().isFailure()).isTrue();
        assertThat(result.future().join().failureReasons()).contains(
                NonEmptyList.of(AccountError.of(AccountError.Reason.InvalidData, "Cannot reserve a negative amount")));
        verifyZeroInteractions(commandApi);
    }

    @Test
    void cancelReservationShouldPublishAndQueryCommand() {
        AccountView accountView = new AccountView();
        accountView.setId(key.id().toString());
        AccountTransactionView transactionView = new AccountTransactionView();
        transactionView.setStatus(Reservation.Status.DRAFT);
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.of(accountView));
        when(accountTransactionRepository
                .findByTransactionKey(new AccountTransactionViewKey(key, reservationId))).thenReturn(Optional.of(transactionView));
        when(commandApi.publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class)))
                .thenReturn(FutureResult.of(Sequence.first()));

        FutureResult<AccountError, Sequence> result = accountWriteService.cancelReservation(key, reservationId);
        assertThat(result.future().join().isSuccess()).isTrue();
        verify(commandApi).publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class));
    }

    @Test
    void cancelReservationShouldReturnErrorWhenReservationNotDraftStatus() {
        AccountView accountView = new AccountView();
        accountView.setId(key.id().toString());
        AccountTransactionView transactionView = new AccountTransactionView();
        transactionView.setStatus(Reservation.Status.CONFIRMED);
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.of(accountView));
        when(accountTransactionRepository
                .findByTransactionKey(new AccountTransactionViewKey(key, reservationId))).thenReturn(Optional.of(transactionView));
        when(commandApi.publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class)))
                .thenReturn(FutureResult.of(Sequence.first()));

        FutureResult<AccountError, Sequence> result = accountWriteService.cancelReservation(key, reservationId);
        assertThat(result.future().join().isFailure()).isTrue();
        assertThat(result.future().join().failureReasons()).contains(
                NonEmptyList.of(AccountError.of(AccountError.Reason.InvalidData, "Account reservation is not in draft state")));
        verifyZeroInteractions(commandApi);
    }

    @Test
    void confirmReservationShouldPublishAndQueryCommand() {
        AccountView accountView = new AccountView();
        accountView.setId(key.id().toString());
        AccountTransactionView transactionView = new AccountTransactionView();
        transactionView.setStatus(Reservation.Status.DRAFT);
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.of(accountView));
        when(accountTransactionRepository
                .findByTransactionKey(new AccountTransactionViewKey(key, reservationId))).thenReturn(Optional.of(transactionView));
        when(commandApi.publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class)))
                .thenReturn(FutureResult.of(Sequence.first()));

        FutureResult<AccountError, Sequence> result = accountWriteService.confirmReservation(key, reservationId, Money.valueOf("100"));
        assertThat(result.future().join().isSuccess()).isTrue();
        verify(commandApi).publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class));
    }

    @Test
    void confirmReservationShouldReturnErrorWhenReservationNotDraftStatus() {
        AccountView accountView = new AccountView();
        accountView.setId(key.id().toString());
        AccountTransactionView transactionView = new AccountTransactionView();
        transactionView.setStatus(Reservation.Status.CONFIRMED);
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.of(accountView));
        when(accountTransactionRepository
                .findByTransactionKey(new AccountTransactionViewKey(key, reservationId))).thenReturn(Optional.of(transactionView));
        when(commandApi.publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class)))
                .thenReturn(FutureResult.of(Sequence.first()));

        FutureResult<AccountError, Sequence> result = accountWriteService.confirmReservation(key, reservationId, Money.valueOf("100"));
        assertThat(result.future().join().isFailure()).isTrue();
        assertThat(result.future().join().failureReasons()).contains(
                NonEmptyList.of(AccountError.of(AccountError.Reason.InvalidData, "Account reservation is not in draft state")));
        verifyZeroInteractions(commandApi);
    }
}