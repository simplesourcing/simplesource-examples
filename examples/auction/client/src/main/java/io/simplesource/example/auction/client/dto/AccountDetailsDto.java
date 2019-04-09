package io.simplesource.example.auction.client.dto;

import java.math.BigDecimal;
import java.util.List;

public final class AccountDetailsDto {
    private String accountId;
    private String userName;
    private BigDecimal funds;
    private BigDecimal availableFunds;
    private long lastEventSequence;
    private List<AccountTransactionDto> draftReservations;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

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

    public BigDecimal getAvailableFunds() {
        return availableFunds;
    }

    public void setAvailableFunds(BigDecimal availableFunds) {
        this.availableFunds = availableFunds;
    }

    public long getLastEventSequence() {
        return lastEventSequence;
    }

    public void setLastEventSequence(long lastEventSequence) {
        this.lastEventSequence = lastEventSequence;
    }

    public List<AccountTransactionDto> getDraftReservations() {
        return draftReservations;
    }

    public void setDraftReservations(List<AccountTransactionDto> draftReservations) {
        this.draftReservations = draftReservations;
    }
}
