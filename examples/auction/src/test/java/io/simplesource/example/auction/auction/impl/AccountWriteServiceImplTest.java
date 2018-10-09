package io.simplesource.example.auction.auction.impl;

import io.simplesource.api.CommandAPI;
import io.simplesource.data.FutureResult;
import io.simplesource.data.Sequence;
import io.simplesource.example.auction.account.domain.Account;
import io.simplesource.example.auction.account.domain.AccountCommand;
import io.simplesource.example.auction.account.domain.AccountError;
import io.simplesource.example.auction.account.domain.AccountKey;
import io.simplesource.example.auction.account.query.repository.AccountRepository;
import io.simplesource.example.auction.account.query.repository.AccountTransactionRepository;
import io.simplesource.example.auction.account.query.views.AccountView;
import io.simplesource.example.auction.account.service.AccountWriteService;
import io.simplesource.example.auction.account.service.AccountWriteServiceImpl;
import io.simplesource.example.auction.core.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    private AccountWriteService accountWriteService;

    @BeforeEach
    void setUp() {
        accountWriteService = new AccountWriteServiceImpl(commandApi, accountRepository, accountTransactionRepository);
        account = new Account("Bob", Money.valueOf("1000"),  Collections.emptyList());
    }

    @Test
    void createShouldReturnErrorWhenAccountWithSameKeyExists() {
        when(accountRepository.findByAccountId(key.id().toString())).thenReturn(Optional.of(mock(AccountView.class)));

        FutureResult<AccountError, Sequence> result = accountWriteService.createAccount(key, account);
        assertThat(result.future().join().isFailure()).isTrue();
    }
}