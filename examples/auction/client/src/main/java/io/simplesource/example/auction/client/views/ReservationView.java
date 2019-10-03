package io.simplesource.example.auction.client.views;

import io.simplesource.example.auction.domain.Reservation;

import java.math.BigDecimal;

public final class ReservationView {
    private final String  reservationId;
    private final long timestamp;
    private final String description;
    private final BigDecimal amount;
    private final Reservation.Status status;

    public ReservationView(String reservationId, Long timestamp, String description, BigDecimal amount, Reservation.Status status) {
        this.reservationId = reservationId;
        this.timestamp = timestamp;
        this.description = description;
        this.amount = amount;
        this.status = status;
    }

    public String getReservationId() {
        return reservationId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Reservation.Status getStatus() {
        return status;
    }
}
