package io.simplesource.example.auction.client.service;

import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.example.auction.client.views.AccountTransactionView;

import java.util.List;

/**
 * Read (query) service for accounts.
 */
public interface AccountReadService {

    /**
     * Return a list of transactions for a given account.
     * @param accountKey account to query.
     * @return list of transactions.
     */
    List<AccountTransactionView> getTransactionHistory(AccountKey accountKey);
}
