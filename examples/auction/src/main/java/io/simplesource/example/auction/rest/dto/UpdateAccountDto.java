package io.simplesource.example.auction.rest.dto;

import javax.validation.constraints.NotBlank;

public final class UpdateAccountDto {
    @NotBlank
    private final String userName;

    public UpdateAccountDto(@NotBlank String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
