package io.simplesource.example.auction.client.views;

import java.math.BigDecimal;

public class BidView {
    String bidder;
    Long timestamp;
    String reservationId;
    BigDecimal amount;

    public String getBidder() {
        return bidder;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getReservationId() {
        return reservationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
