package io.simplesource.example.auction.rest.dto;

import java.math.BigDecimal;

public final class AccountDto {
    private final String userName;
    private final BigDecimal funds;

    public AccountDto(String userName, BigDecimal funds) {
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
