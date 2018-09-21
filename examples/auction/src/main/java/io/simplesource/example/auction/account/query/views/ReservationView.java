package io.simplesource.example.auction.account.query.views;

import io.simplesource.example.auction.account.domain.Reservation;

import java.math.BigDecimal;

public final class ReservationView {
    private String  reservationId;
    private String description;
    private BigDecimal amount;
    private Reservation.Status status;

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Reservation.Status getStatus() {
        return status;
    }

    public ReservationView setStatus(Reservation.Status status) {
        this.status = status;
        return this;
    }
}
