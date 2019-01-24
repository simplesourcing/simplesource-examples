package io.simplesource.example.auction.rest.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class ConfirmReservationDto {
    @NotNull
    private final BigDecimal amount;

    public ConfirmReservationDto(@NotNull BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
