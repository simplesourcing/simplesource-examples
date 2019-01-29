package io.simplesource.example.auction.account.service;

import io.simplesource.data.FutureResult;
import io.simplesource.data.Sequence;
import io.simplesource.example.auction.account.domain.*;
import io.simplesource.example.auction.core.Money;

public interface AccountWriteService {

    /**
     * Create a new account. The username must be unique.
     * @param accountKey account identifier.
     * @param account to create.
     * @return FutureResult with the command sequence number or validation error.
     */
    FutureResult<AccountError, Sequence> createAccount(AccountKey accountKey, Account account);


    /**
     * Update the username for an account.
     * @param accountKey account identifier.
     * @param username to set.
     * @return FutureResult with the command sequence number or validation error.
     */
    FutureResult<AccountError, Sequence> updateAccount(AccountKey accountKey, String username);

    /**
     * Add funds to an existing account.
     * @param accountKey account identifier.
     * @param funds to add (cannot be negative).
     * @return FutureResult with the command sequence number or validation error.
     */
    FutureResult<AccountError, Sequence> addFunds(AccountKey accountKey, Money funds);

    /**
     * Reserve funds from an existing account. Funds are on hold until the reservation is either
     * confirmed or cancelled. The account should have sufficient funds.
     * @param accountKey account identifier.
     * @param reservationId reservation identifier.
     * @param reservation to reserve.
     * @return FutureResult with the command sequence number or validation error.
     */
    FutureResult<AccountError, Sequence> reserveFunds(AccountKey accountKey, ReservationId reservationId, Reservation reservation);

    /**
     * Cancel a reservation for an account. Funds will be added back to the account.
     * @param accountKey account identifier.
     * @param reservationId reservation to cancel.
     * @return FutureResult with the command sequence number or validation error.
     */
    FutureResult<AccountError, Sequence> cancelReservation(AccountKey accountKey, ReservationId reservationId);

    /**
     * Confirm a reservation for an account. Funds will be deducted from the account.
     * @param accountKey account identifier.
     * @param reservationId reservation to confirm.
     * @return FutureResult with the command sequence number or validation error.
     */
    FutureResult<AccountError, Sequence> confirmReservation(AccountKey accountKey, ReservationId reservationId, Money amount);
}
