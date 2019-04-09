package io.simplesource.example.auction.client.dto;

import javax.validation.constraints.NotBlank;

public final class UpdateAccountDto {
    @NotBlank
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
