package io.simplesource.example.auction.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public final class ReserveFundsDto {
    private final UUID reservationId;
    private final BigDecimal amount;
    private final String description;

    public ReserveFundsDto(UUID reservationId, BigDecimal amount, String description) {
        this.reservationId = reservationId;
        this.amount = amount;
        this.description = description;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }
}
