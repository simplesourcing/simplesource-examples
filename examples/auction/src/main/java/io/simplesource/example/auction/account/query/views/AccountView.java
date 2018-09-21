package io.simplesource.example.auction.account.query.views;

import io.simplesource.example.auction.account.domain.Reservation;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.List;

@Document(collection = "auction_account")
public class AccountView {
    @Id
    private String id;
    @Field("value.username")
    private String userName;
    @Field("value.funds")
    private BigDecimal funds = BigDecimal.ZERO;
    private BigDecimal availableFunds;
    @Field("sequence")
    private long lastEventSequence;
    @Field("value.draftReservations")
    private List<ReservationView> draftReservations;

    public BigDecimal getAvailableFunds() {
        if ( draftReservations == null ) {
            return funds;
        }
        BigDecimal reservedFunds = draftReservations.stream().filter(r -> r.getStatus() == Reservation.Status.DRAFT)
                .map(ReservationView::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return funds.subtract(reservedFunds);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getFunds() {
        return funds;
    }

    public void setFunds(BigDecimal funds) {
        this.funds = funds;
    }

    public List<ReservationView> getDraftReservations() {
        return draftReservations;
    }

    public long getLastEventSequence() {
        return lastEventSequence;
    }

    public AccountView setLastEventSequence(long lastEventSequence) {
        this.lastEventSequence = lastEventSequence;
        return this;
    }

    public void setDraftReservations(List<ReservationView> draftReservations) {
        this.draftReservations = draftReservations;
    }
}
