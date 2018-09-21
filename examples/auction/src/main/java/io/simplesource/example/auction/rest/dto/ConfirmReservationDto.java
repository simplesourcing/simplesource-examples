package io.simplesource.example.auction.rest.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class ConfirmReservationDto {
    @NotNull
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
