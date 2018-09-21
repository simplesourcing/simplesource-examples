package io.simplesource.example.auction.rest.dto;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public final class CreateAuctionDto {
    @NotNull
    private UUID key;
    @NotNull
    private AuctionDto value;

    public UUID getKey() {
        return key;
    }

    public CreateAuctionDto setKey(UUID key) {
        this.key = key;
        return this;
    }

    public AuctionDto getValue() {
        return value;
    }

    public CreateAuctionDto setValue(AuctionDto value) {
        this.value = value;
        return this;
    }
}
