package io.simplesource.example.auction.account.service;

import io.simplesource.data.FutureResult;
import io.simplesource.data.Sequence;
import io.simplesource.example.auction.account.domain.*;
import io.simplesource.example.auction.core.Money;

public interface AccountWriteService {
    FutureResult<AccountError, Sequence> createAccount(AccountKey accountKey, Account account);
    FutureResult<AccountError, Sequence> updateAccount(AccountKey accountKey, String username);
    FutureResult<AccountError, Sequence> addFunds(AccountKey accountKey, Money funds);
    FutureResult<AccountError, Sequence> reserveFunds(AccountKey accountKey, ReservationId reservationId, Reservation reservation);
    FutureResult<AccountError, Sequence> cancelReservation(AccountKey accountKey, ReservationId reservationId);
    FutureResult<AccountError, Sequence> confirmReservation(AccountKey accountKey, ReservationId reservationId, Money amount);
}
