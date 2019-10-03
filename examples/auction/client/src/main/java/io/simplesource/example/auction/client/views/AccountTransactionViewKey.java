package io.simplesource.example.auction.client.views;

import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.example.auction.domain.ReservationId;
import lombok.Value;

public final class AccountTransactionViewKey {
    private final AccountKey accountKey;
    private final ReservationId reservationId;

    public AccountTransactionViewKey(AccountKey accountKey, ReservationId reservationId) {
        this.accountKey = accountKey;
        this.reservationId = reservationId;
    }

    public AccountKey getAccountKey() {
        return accountKey;
    }

    public ReservationId getReservationId() {
        return reservationId;
    }
}
