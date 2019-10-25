package io.simplesource.example.demo.service;

import io.simplesource.data.Sequence;
import io.simplesource.example.demo.domain.AccountSummary;
import io.simplesource.example.demo.domain.AccountTransaction;
import io.simplesource.example.demo.repository.read.AccountReadRepository;
import io.simplesource.example.demo.repository.write.AccountWriteRepository;
import io.simplesource.example.demo.repository.write.CreateAccountError;

import java.util.List;
import java.util.Optional;


public class DefaultAccountService implements AccountService {
    private final AccountReadRepository accountReadRepository;
    private final AccountWriteRepository accountWriteRepository;

    public DefaultAccountService(AccountReadRepository accountReadRepository, AccountWriteRepository accountWriteRepository) {
        this.accountReadRepository = accountReadRepository;
        this.accountWriteRepository = accountWriteRepository;
    }


    @Override
    public boolean accountExists(String accountName) {
        return accountReadRepository.accountSummary(accountName).isPresent();
    }

    @Override
    public Optional<AccountSummary> getAccountSummary(String accountName) {
        return accountReadRepository.accountSummary(accountName);
    }

    @Override
    public Optional<CreateAccountError> createAccount(String name, double openingBalance) {
        return accountWriteRepository.create(name, openingBalance);
    }

    @Override
    public List<AccountSummary> list() {
        return accountReadRepository.list();
    }

    @Override
    public void deposit(String account, double amount, long sequence) {
        accountWriteRepository.deposit(account, amount, Sequence.position(sequence));
    }

    @Override
    public void withdraw(String account, double amount, long sequence) {
        accountWriteRepository.withdraw(account, amount, Sequence.position(sequence));
    }

    @Override
    public List<AccountTransaction> getTransactions(String account) {
        return accountReadRepository.getTransactions(account);
    }

}
