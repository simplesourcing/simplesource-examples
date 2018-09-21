package io.simplesource.example.auction.account.domain;

import lombok.Value;

@Value
public final class AccountTransactionKey {
    private final AccountKey accountKey;
    private final ReservationId reservationId;
}
