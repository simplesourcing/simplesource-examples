package io.simplesource.example.auction.client.dto;

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
