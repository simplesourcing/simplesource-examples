package io.simplesource.example.auction.client.dto;

import io.simplesource.example.auction.domain.Reservation;

import java.math.BigDecimal;

public final class AccountTransactionDto {
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

    public void setStatus(Reservation.Status status) {
        this.status = status;
    }
}
