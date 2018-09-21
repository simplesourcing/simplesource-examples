package io.simplesource.example.auction.account.domain;

import lombok.Value;

import java.util.UUID;

@Value
public final class ReservationId {
    private final UUID id;

    public static ReservationId of(String id) {
        return new ReservationId(id);
    }

    public ReservationId(UUID id) {
        this.id = id;
    }
    public ReservationId(String id) {
        this.id = UUID.fromString(id);
    }

    public String asString() {
        return id.toString();
    }
}
