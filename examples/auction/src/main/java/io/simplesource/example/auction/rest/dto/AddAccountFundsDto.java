package io.simplesource.example.auction.rest.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class AddAccountFundsDto {
    @NotNull
    private BigDecimal funds;

    public BigDecimal getFunds() {
        return funds;
    }

    public void setFunds(BigDecimal funds) {
        this.funds = funds;
    }
}
