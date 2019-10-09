package io.simplesource.example.auction.client.views;

import io.simplesource.example.auction.domain.Reservation;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

@Document(collection = "auction_account_transactions")
public final class AccountTransactionView {
    @Id
    private AccountTransactionId id;

    @Field("_id.accountId")
    private String accountId;
    @Field("_id.reservationId")
    private String reservationId;
    @Field("timestamp")
    private Long timestamp;
    @Field("description")
    private String description;
    @Field("amount")
    private BigDecimal amount;
    @Field("status")
    private Reservation.Status status;


    public String getAccountId() {
        return accountId;
    }

    public AccountTransactionView setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getReservationId() {
        return reservationId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public AccountTransactionView setDescription(String description) {
        this.description = description;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public AccountTransactionView setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public Reservation.Status getStatus() {
        return status;
    }

    public AccountTransactionView setStatus(Reservation.Status status) {
        this.status = status;
        return this;
    }

    public static class AccountTransactionId {
        private String accountId;
        private String reservationId;

        public AccountTransactionId(String accountId, String reservationId) {
            this.accountId = accountId;
            this.reservationId = reservationId;
        }

        public AccountTransactionId() {
        }

        public String getAccountId() {
            return accountId;
        }

        public AccountTransactionId setAccountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public String getReservationId() {
            return reservationId;
        }

        public AccountTransactionId setReservationId(String reservationId) {
            this.reservationId = reservationId;
            return this;
        }
    }
}
