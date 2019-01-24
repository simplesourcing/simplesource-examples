package io.simplesource.example.auction.rest.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class AddAccountFundsDto {
    @NotNull
    private final BigDecimal funds;

    public AddAccountFundsDto(@NotNull BigDecimal funds) {
        this.funds = funds;
    }

    public BigDecimal getFunds() {
        return funds;
    }
}
