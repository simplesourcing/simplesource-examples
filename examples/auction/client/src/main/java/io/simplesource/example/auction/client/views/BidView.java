package io.simplesource.example.auction.client.views;

import java.math.BigDecimal;

public class BidView {
    private final String bidder;
    private final long timestamp;
    private final String reservationId;
    private final BigDecimal amount;

    public BidView(String bidder, long timestamp, String reservationId, BigDecimal amount) {
        this.bidder = bidder;
        this.timestamp = timestamp;
        this.reservationId = reservationId;
        this.amount = amount;
    }

    public String getBidder() {
        return bidder;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getReservationId() {
        return reservationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
