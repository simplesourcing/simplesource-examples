package io.simplesource.example.auction.rest.dto;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public final class CreateAccountDto {
    @NotNull
    private UUID accountId;
    @NotNull
    private AccountDto accountDto;

    public CreateAccountDto(@NotNull UUID accountId, @NotNull AccountDto accountDto) {
        this.accountId = accountId;
        this.accountDto = accountDto;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public AccountDto getAccountDto() {
        return accountDto;
    }
}
