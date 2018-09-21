package io.simplesource.example.auction.account.service;

import io.simplesource.example.auction.account.domain.AccountKey;
import io.simplesource.example.auction.account.query.repository.AccountTransactionRepository;
import io.simplesource.example.auction.account.query.views.AccountTransactionView;

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
