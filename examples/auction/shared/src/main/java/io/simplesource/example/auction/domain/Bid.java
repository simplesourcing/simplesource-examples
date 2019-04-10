package io.simplesource.example.auction.domain;

import io.simplesource.example.auction.core.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public final class Bid {
    private ReservationId reservationId;
    private Instant timestamp;
    private AccountKey bidder;
    private Money amount;
}
