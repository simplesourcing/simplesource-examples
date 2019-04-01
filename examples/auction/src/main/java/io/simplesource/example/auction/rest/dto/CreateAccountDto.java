package io.simplesource.example.auction.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public final class CreateAccountDto {
    @NotNull
    private UUID accountId;
    @NotNull
    private AccountDto accountDto;

    @JsonCreator
    public CreateAccountDto(@JsonProperty("accountId") @NotNull UUID accountId, @JsonProperty("accountDto") @NotNull AccountDto accountDto) {
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
