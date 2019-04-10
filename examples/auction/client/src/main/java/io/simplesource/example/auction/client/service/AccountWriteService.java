package io.simplesource.example.auction.client.service;

import io.simplesource.data.FutureResult;
import io.simplesource.data.Sequence;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.*;
import io.simplesource.saga.model.messages.SagaResponse;

/**
 * Write (command) service for accounts.
 */
public interface AccountWriteService {

    /**
     * Create a new account. The username must be unique.
     * @param accountKey account identifier.
     * @param account to create.
     * @return FutureResult with the command sequence number or validation error.
     */
    FutureResult<AccountError, SagaResponse> createAccount(AccountKey accountKey, Account account);

    /**
     * Update the username for an account.
     * @param accountKey account identifier.
     * @param username to set.
     * @return FutureResult with the command sequence number or validation error.
     */
    FutureResult<AccountError, SagaResponse> updateAccount(AccountKey accountKey, String username);

    /**
     * Add funds to an existing account.
     * @param accountKey account identifier.
     * @param funds to add (cannot be negative).
     * @return FutureResult with the command sequence number or validation error.
     */
    FutureResult<AccountError, Sequence> addFunds(AccountKey accountKey, Money funds);
}
