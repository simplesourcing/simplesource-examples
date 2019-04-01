package io.simplesource.example.auction.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class AddAccountFundsDto {
    @NotNull
    private final BigDecimal funds;

    @JsonCreator
    public AddAccountFundsDto(@JsonProperty("funds") @NotNull BigDecimal funds) {
        this.funds = funds;
    }

    public BigDecimal getFunds() {
        return funds;
    }
}
