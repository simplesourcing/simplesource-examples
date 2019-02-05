package io.simplesource.example.auction.rest.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public final class AccountDetailsDto {
    private final String accountId;
    private final String userName;
    private final BigDecimal funds;
    private final BigDecimal availableFunds;
    private final long lastEventSequence;
    private final List<AccountTransactionDto> draftReservations;

    public AccountDetailsDto(String accountId, String userName, BigDecimal funds, BigDecimal availableFunds, long lastEventSequence, List<AccountTransactionDto> draftReservations) {
        this.accountId = accountId;
        this.userName = userName;
        this.funds = funds;
        this.availableFunds = availableFunds;
        this.lastEventSequence = lastEventSequence;
        this.draftReservations = Collections.unmodifiableList(draftReservations);
    }

    public String getAccountId() {
        return accountId;
    }
    public String getUserName() {
        return userName;
    }
    public BigDecimal getFunds() {
        return funds;
    }
    public BigDecimal getAvailableFunds() {
        return availableFunds;
    }
    public long getLastEventSequence() {
        return lastEventSequence;
    }
    public List<AccountTransactionDto> getDraftReservations() {
        return draftReservations;
    }
}
