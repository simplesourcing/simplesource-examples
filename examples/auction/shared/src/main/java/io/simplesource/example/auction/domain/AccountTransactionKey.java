package io.simplesource.example.auction.domain;

import lombok.Value;

@Value
public final class AccountTransactionKey {
    private final AccountKey accountKey;
    private final ReservationId reservationId;
}
