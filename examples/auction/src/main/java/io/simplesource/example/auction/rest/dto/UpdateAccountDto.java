package io.simplesource.example.auction.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;

public final class UpdateAccountDto {
    @NotBlank
    private final String userName;

    @JsonCreator
    public UpdateAccountDto(@JsonProperty("userName") @NotBlank String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
