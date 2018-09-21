package io.simplesource.example.auction.account.query.views;

import io.simplesource.example.auction.account.domain.AccountKey;
import io.simplesource.example.auction.account.domain.ReservationId;
import lombok.Value;

@Value
public final class AccountTransactionViewKey {
    final AccountKey accountKey;
    final ReservationId reservationId;
}
