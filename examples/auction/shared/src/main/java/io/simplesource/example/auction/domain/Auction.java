package io.simplesource.example.auction.domain;

import io.simplesource.example.auction.auction.wire.AuctionStatus;
import io.simplesource.example.auction.core.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public final class Auction {
    @NonNull
    private final String creator;
    @NonNull
    private final String title;
    @NonNull
    private final String description;
    @NonNull
    private Money reservePrice;
    private Money price;
    @NonNull
    private Duration duration;
    @NonNull
    private AuctionStatus status;
    private Instant start;
    private AccountKey winner;

    @Builder.Default
    private List<Bid> bids = Collections.emptyList();

    public Auction addBid(Bid bid) {
        return new Auction(creator, title, description, reservePrice, bid.amount(), duration, status, start, winner,
                Stream.concat(bids.stream(), Stream.of(bid)).collect(Collectors.toList()));
    }

    public Instant end() {
        return start.plus(duration);
    }

    public Optional<Bid> winningBid() {
        return bids.isEmpty() ?
                Optional.empty() :
                Optional.of(bids.get(bids.size() - 1));
    }
}
