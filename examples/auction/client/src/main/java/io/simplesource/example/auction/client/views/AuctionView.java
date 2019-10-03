package io.simplesource.example.auction.client.views;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;


import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Document(collection = "auction_auction")
public class AuctionView {
    @Id
    private final String id;

    @Field("value.creator")
    private final String creator;

    @Field("value.title")
    private final String title;

    @Field("value.description")
    private final String description;

    @Field("value.reservePrice")
    private final BigDecimal reservePrice;

    @Field("value.price")
    private final BigDecimal price;

    @Field("value.status")
    private final String status;

    @Field("value.start")
    private final Long start;

    @Field("value.duration")
    private final long duration;

    @Field("value.winner")
    private final String winner;

    @Field("sequence")
    private long lastEventSequence;

    @Field("value.bids")
    private final List<BidView> bids;


    public AuctionView(String id, String creator, String title, String description, BigDecimal reservePrice, BigDecimal price, String status, Long start, long duration, String winner, long lastEventSequence, List<BidView> bids) {
        this.id = id;
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.reservePrice = reservePrice;
        this.price = price;
        this.status = status;
        this.start = start;
        this.duration = duration;
        this.winner = winner;
        this.lastEventSequence = lastEventSequence;
        this.bids = Collections.unmodifiableList(bids);
    }

    public String getId() {
        return id;
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

    public BigDecimal getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public Long getStart() {
        return start;
    }

    public long getDuration() {
        return duration;
    }

    public String getWinner() {
        return winner;
    }

    public long getLastEventSequence() {
        return lastEventSequence;
    }

    public List<BidView> getBids() {
        return bids;
    }
}
