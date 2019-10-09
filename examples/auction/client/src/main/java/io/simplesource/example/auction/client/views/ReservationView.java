package io.simplesource.example.auction.client.views;

import io.simplesource.example.auction.domain.Reservation;

import java.math.BigDecimal;

public final class ReservationView {
    private String  reservationId;
    private Long timestamp;
    private String description;
    private BigDecimal amount;
    private Reservation.Status status;

    public String getReservationId() {
        return reservationId;
    }

    public Long getTimestamp() {
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
