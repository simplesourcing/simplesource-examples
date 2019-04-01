package io.simplesource.example.auction.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public final class AccountDto {
    private final String userName;
    private final BigDecimal funds;

    @JsonCreator
    public AccountDto(@JsonProperty("userName") String userName, @JsonProperty("funds") BigDecimal funds) {
        this.userName = userName;
        this.funds = funds;
    }

    public String getUserName() {
        return userName;
    }
    public BigDecimal getFunds() {
        return funds;
    }
}
