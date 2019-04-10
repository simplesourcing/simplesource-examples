package io.simplesource.example.auction.domain;

import lombok.ToString;
import lombok.Value;

import java.util.UUID;

@Value
@ToString(includeFieldNames = false)
public final class AuctionKey {
    @ToString.Include
    private final UUID id;

    public static AuctionKey of(String id) {
        return new AuctionKey(id);
    }

    public AuctionKey(UUID id) {
        this.id = id;
    }
    public AuctionKey(String id) {
        this.id = UUID.fromString(id);
    }

    public String asString() {
        return id.toString();
    }
}
