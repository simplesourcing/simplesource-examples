package io.simplesource.example.auction.client.dto;

import java.math.BigDecimal;

public final class AccountDto {
    private String userName;
    private BigDecimal funds;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getFunds() {
        return funds;
    }

    public void setFunds(BigDecimal funds) {
        this.funds = funds;
    }
}
