package io.simplesource.example.auction.account.service;

import io.simplesource.example.auction.account.domain.AccountKey;
import io.simplesource.example.auction.account.query.views.AccountTransactionView;

import java.util.List;

public interface AccountReadService {
    List<AccountTransactionView> getTransactionHistory(AccountKey accountKey);
}
