package io.simplesource.example.auction.client.service;

import io.simplesource.api.CommandAPI;
import io.simplesource.data.FutureResult;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Result;
import io.simplesource.data.Sequence;
import io.simplesource.example.auction.client.repository.AccountRepository;
import io.simplesource.example.auction.client.views.AccountView;
import io.simplesource.example.auction.command.AccountCommand;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.Account;
import io.simplesource.example.auction.domain.AccountError;
import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.saga.model.api.SagaAPI;
import io.simplesource.saga.model.messages.SagaRequest;
import io.simplesource.saga.model.messages.SagaResponse;
import io.simplesource.saga.model.saga.SagaId;
import org.apache.avro.generic.GenericRecord;
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
    private SagaAPI<GenericRecord> sagaApi;

    private AccountWriteService accountWriteService;

    private Account account = new Account("Bob", Money.valueOf("1000"), Collections.emptyList());
    private AccountKey key = new AccountKey(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        accountWriteService = new AccountWriteServiceImpl(commandApi, sagaApi, accountRepository);
    }

    @Test
    void createShouldPublishAndQuerySaga() {
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.empty());
        when(sagaApi.submitSaga(any(SagaRequest.class)))
                .thenReturn(FutureResult.of(SagaId.random()));
        when(sagaApi.getSagaResponse(any(SagaId.class), any(Duration.class)))
                .thenReturn(FutureResult.of(SagaResponse.of(SagaId.random(), Result.success(Sequence.first()))));

        FutureResult<AccountError, SagaResponse> result = accountWriteService.createAccount(key, account);
        assertThat(result.future().join().isSuccess()).isTrue();
        verify(sagaApi).submitSaga(any(SagaRequest.class));
    }

    @Test
    void createShouldReturnErrorWhenAccountWithSameKeyExists() {
        AccountView accountView = new AccountView();
        accountView.setId(key.id().toString());
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.of(accountView));

        FutureResult<AccountError, SagaResponse> result = accountWriteService.createAccount(key, account);
        assertThat(result.future().join().isFailure()).isTrue();
        assertThat(result.future().join().failureReasons()).contains(
                NonEmptyList.of(new AccountError.AccountIdAlreadyExist(String.format("Account ID %s already exist", key.asString()))));
        verifyZeroInteractions(commandApi);
        verifyZeroInteractions(sagaApi);
    }

    @Test
    void createShouldReturnErrorWhenFundsNegative() {
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.empty());

        Account account = new Account("Bob", Money.valueOf("-1000"), Collections.emptyList());
        FutureResult<AccountError, SagaResponse> result = accountWriteService.createAccount(key, account);
        assertThat(result.future().join().isFailure()).isTrue();
        assertThat(result.future().join().failureReasons()).contains(
                NonEmptyList.of(new AccountError.InvalidData("Initial funds can not be negative")));
        verifyZeroInteractions(commandApi);
        verifyZeroInteractions(sagaApi);
    }

    @Test
    void updateShouldPublishAndQuerySaga() {
        AccountView accountView = new AccountView();
        accountView.setId(key.id().toString());
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.of(accountView));
        when(sagaApi.submitSaga(any(SagaRequest.class)))
                .thenReturn(FutureResult.of(SagaId.random()));
        when(sagaApi.getSagaResponse(any(SagaId.class), any(Duration.class)))
                .thenReturn(FutureResult.of(SagaResponse.of(SagaId.random(), Result.success(Sequence.first()))));

        FutureResult<AccountError, SagaResponse> result = accountWriteService.updateAccount(key, "Alice");
        assertThat(result.future().join().isSuccess()).isTrue();
        verify(sagaApi).submitSaga(any(SagaRequest.class));
        verifyZeroInteractions(commandApi);
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
                NonEmptyList.of(new AccountError.InvalidData("Cannot add a negative amount")));
    }
}
