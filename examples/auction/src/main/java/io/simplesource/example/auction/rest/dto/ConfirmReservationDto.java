package io.simplesource.example.auction.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class ConfirmReservationDto {
    @NotNull
    private final BigDecimal amount;

    @JsonCreator
    public ConfirmReservationDto(@JsonProperty("amount") @NotNull BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
