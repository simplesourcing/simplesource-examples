package io.simplesource.example.auction.rest.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class AuctionDto {
    @NotNull
    private final String creator;
    @NotNull
    private final String title;
    @NotNull
    private final String description;
    @NotNull
    private final BigDecimal reservePrice;

    public AuctionDto(@NotNull String creator, @NotNull String title, @NotNull String description, @NotNull BigDecimal reservePrice) {
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.reservePrice = reservePrice;
    }

    public String getCreator() {
        return creator;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getReservePrice() {
        return reservePrice;
    }

}
