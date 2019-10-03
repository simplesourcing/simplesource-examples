package io.simplesource.example.auction.client.views;

import io.simplesource.example.auction.domain.Reservation;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.math.BigDecimal;

@Document(collection = "auction_account_transactions")
public final class AccountTransactionView {
    @Id
    private final AccountTransactionId id;

    @Field("_id.accountId")
    private final String accountId;
    @Field("_id.reservationId")
    private final String reservationId;
    @Field("timestamp")
    private final long timestamp;
    @Field("description")
    private final String description;
    @Field("amount")
    private final BigDecimal amount;
    @Field("status")
    private final Reservation.Status status;

    public AccountTransactionView(AccountTransactionId id, String accountId, String reservationId, Long timestamp, String description, BigDecimal amount, Reservation.Status status) {
        this.id = id;
        this.accountId = accountId;
        this.reservationId = reservationId;
        this.timestamp = timestamp;
        this.description = description;
        this.amount = amount;
        this.status = status;
    }

    public String getAccountId() {
        return accountId;
    }


    public String getReservationId() {
        return reservationId;
    }

    public Long getTimestamp() {
        return timestamp;
    }


    public String getDescription() {
        return description;
    }


    public BigDecimal getAmount() {
        return amount;
    }

    public Reservation.Status getStatus() {
        return status;
    }

    public static class AccountTransactionId {
        private String accountId;
        private String reservationId;

        public AccountTransactionId(String accountId, String reservationId) {
            this.accountId = accountId;
            this.reservationId = reservationId;
        }


        public String getAccountId() {
            return accountId;
        }


        public String getReservationId() {
            return reservationId;
        }

    }
}
