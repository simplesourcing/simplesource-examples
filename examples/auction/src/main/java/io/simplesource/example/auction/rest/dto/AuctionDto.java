package io.simplesource.example.auction.rest.dto;

import io.simplesource.example.auction.core.Money;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class AuctionDto {
    @NotNull
    private String creator;
    @NotNull
    private String title;
    @NotNull
    private String description;
    @NotNull
    private BigDecimal reservePrice;

    public String getCreator() {
        return creator;
    }

    public AuctionDto setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public AuctionDto setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AuctionDto setDescription(String description) {
        this.description = description;
        return this;
    }

    public BigDecimal getReservePrice() {
        return reservePrice;
    }

    public AuctionDto setReservePrice(BigDecimal reservePrice) {
        this.reservePrice = reservePrice;
        return this;
    }
}
