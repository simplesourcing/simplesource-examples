package io.simplesource.example.auction.client.service;

import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.example.auction.client.repository.AccountTransactionRepository;
import io.simplesource.example.auction.client.views.AccountTransactionView;

import javax.validation.constraints.NotNull;
import java.util.List;


public final class AccountReadServiceImpl implements AccountReadService {
    private final AccountTransactionRepository accountTransactionRepository;

    public AccountReadServiceImpl(AccountTransactionRepository accountTransactionRepository) {
        this.accountTransactionRepository = accountTransactionRepository;
    }

    @Override
    public List<AccountTransactionView> getTransactionHistory(@NotNull AccountKey accountKey) {
        return accountTransactionRepository.findByAccountId(accountKey.asString());
    }
}
