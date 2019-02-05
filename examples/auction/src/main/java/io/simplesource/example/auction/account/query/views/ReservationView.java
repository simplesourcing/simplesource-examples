package io.simplesource.example.auction.account.query.views;

import io.simplesource.example.auction.account.domain.Reservation;

import java.math.BigDecimal;

public final class ReservationView {
    private final String  reservationId;
    private final String description;
    private final BigDecimal amount;
    private final Reservation.Status status;

    public ReservationView(String reservationId, String description, BigDecimal amount, Reservation.Status status) {
        this.reservationId = reservationId;
        this.description = description;
        this.amount = amount;
        this.status = status;
    }

    public String getReservationId() {
        return reservationId;
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
