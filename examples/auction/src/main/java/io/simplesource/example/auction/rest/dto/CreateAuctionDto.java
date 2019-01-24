package io.simplesource.example.auction.rest.dto;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public final class CreateAuctionDto {
    @NotNull
    private final UUID key;
    @NotNull
    private final AuctionDto value;

    public CreateAuctionDto(@NotNull UUID key, @NotNull AuctionDto value) {
        this.key = key;
        this.value = value;
    }

    public UUID getKey() {
        return key;
    }

    public AuctionDto getValue() {
        return value;
    }

}
