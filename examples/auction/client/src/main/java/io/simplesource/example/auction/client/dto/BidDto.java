package io.simplesource.example.auction.client.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public final class BidDto {
    @NotNull
    private UUID reservationId;
    @NotNull
    private UUID accountId;
    @NotNull
    private BigDecimal amount;

    public UUID getReservationId() {
        return reservationId;
    }

    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
