package io.simplesource.example.auction.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public final class ReserveFundsDto {
    private final UUID reservationId;
    private final BigDecimal amount;
    private final String description;

    @JsonCreator
    public ReserveFundsDto(@JsonProperty("reservationId") UUID reservationId, @JsonProperty("amount") BigDecimal amount, @JsonProperty("description") String description) {
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
