package io.simplesource.example.auction.client.views;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Document(collection = "auction_auction")
public class AuctionView {
    @Id
    private String id;
    @Field("value.creator")
    private String creator;
    @Field("value.title")
    private String title;
    @Field("value.description")
    private String description;
    @Field("value.reservePrice")
    private BigDecimal reservePrice;
    @Field("value.price")
    private BigDecimal price;
    @Field("value.status")
    private String status;
    @Field("value.start")
    private Long start;
    @Field("value.duration")
    private Long duration;
    @Field("value.winner")
    private String winner;
    @Field("sequence")
    private long lastEventSequence;
    @Field("value.bids")
    private List<BidView> bids;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public long getLastEventSequence() {
        return lastEventSequence;
    }

    public String getStatus() {
        return status;
    }

    public Long getStart() {
        return start;
    }

    public Long getDuration() {
        return duration;
    }

    public String getWinner() {
        return winner;
    }

    public List<BidView> getBids() {
        return bids;
    }
}
