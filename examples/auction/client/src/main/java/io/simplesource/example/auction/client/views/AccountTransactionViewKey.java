package io.simplesource.example.auction.client.views;

import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.example.auction.domain.ReservationId;
import lombok.Value;

@Value
public final class AccountTransactionViewKey {
    final AccountKey accountKey;
    final ReservationId reservationId;
}
