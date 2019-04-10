package io.simplesource.example.auction.client.dto;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public final class CreateAuctionDto {
    @NotNull
    private UUID auctionId;
    @NotNull
    private AuctionDto auctionDto;

    public UUID getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(UUID auctionId) {
        this.auctionId = auctionId;
    }

    public AuctionDto getAuctionDto() {
        return auctionDto;
    }

    public void setAuctionDto(AuctionDto auctionDto) {
        this.auctionDto = auctionDto;
    }
}
