package io.simplesource.example.auction.account.service;

import io.simplesource.api.CommandAPI;
import io.simplesource.data.Sequence;
import io.simplesource.data.FutureResult;
import io.simplesource.example.auction.account.domain.Account;
import io.simplesource.example.auction.account.domain.AccountKey;
import io.simplesource.example.auction.account.domain.Reservation;
import io.simplesource.example.auction.account.domain.ReservationId;
import io.simplesource.example.auction.core.Money;

import java.util.UUID;

public interface AccountWriteService {
    FutureResult<CommandAPI.CommandError, Sequence> createAccount(AccountKey accountKey, Account account);
    FutureResult<CommandAPI.CommandError, UUID> updateAccount(AccountKey accountKey, String username);
    FutureResult<CommandAPI.CommandError, Sequence> addFunds(AccountKey accountKey, Money funds);
    FutureResult<CommandAPI.CommandError, Sequence> reserveFunds(AccountKey accountKey, ReservationId reservationId, Reservation reservation);
    FutureResult<CommandAPI.CommandError, Sequence> cancelReservation(AccountKey accountKey, ReservationId reservationId);
    FutureResult<CommandAPI.CommandError, Sequence> confirmReservation(AccountKey accountKey, ReservationId reservationId, Money amount);
}
