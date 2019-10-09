package io.simplesource.example.auction.client.views;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.math.BigDecimal;
import java.util.List;

@Document(indexName = "auction_auction")
public class AuctionView {
    @Id
    private String id;
    @Field
    private String creator;
    @Field
    private String title;
    @Field
    private String description;
    @Field
    private BigDecimal reservePrice;
    @Field
    private BigDecimal price;
    @Field
    private String status;
    @Field
    private Long start;
    @Field
    private Long duration;
    @Field
    private String winner;
    @Field
    private long lastEventSequence;
    @Field
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
