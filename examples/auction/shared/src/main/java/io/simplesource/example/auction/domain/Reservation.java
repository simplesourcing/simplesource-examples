package io.simplesource.example.auction.domain;

import io.simplesource.example.auction.core.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public final class Reservation {
    @NonNull
    private ReservationId reservationId;
    @NonNull
    private Instant timestamp;
    @NonNull
    private AuctionKey auction;
    @NonNull
    private Money amount;
    @NonNull
    private String description;
    private Status status;

    public enum Status {
        DRAFT,
        CANCELLED,
        CONFIRMED
    }
}
