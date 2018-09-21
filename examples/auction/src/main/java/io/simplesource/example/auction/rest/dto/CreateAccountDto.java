package io.simplesource.example.auction.rest.dto;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public final class CreateAccountDto {
    @NotNull
    private UUID accountId;
    @NotNull
    private AccountDto accountDto;

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public AccountDto getAccountDto() {
        return accountDto;
    }

    public void setAccountDto(AccountDto accountDto) {
        this.accountDto = accountDto;
    }
}
