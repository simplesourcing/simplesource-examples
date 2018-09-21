package io.simplesource.example.auction.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public final class ReserveFundsDto {
    private UUID reservationId;
    private BigDecimal amount;
    private String description;

    public UUID getReservationId() {
        return reservationId;
    }

    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
