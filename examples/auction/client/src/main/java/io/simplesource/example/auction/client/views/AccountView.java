package io.simplesource.example.auction.client.views;

import io.simplesource.example.auction.domain.Reservation;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.math.BigDecimal;
import java.util.List;

@Document(indexName = "auction_account")
public class AccountView {
    @Id
    private String id;
    @Field
    private String userName;
    @Field
    private BigDecimal funds = BigDecimal.ZERO;
    private BigDecimal availableFunds;
    @Field
    private long lastEventSequence;
    @Field
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

    public BigDecimal getFunds() {
        return funds;
    }

    public List<ReservationView> getDraftReservations() {
        return draftReservations;
    }

    public long getLastEventSequence() {
        return lastEventSequence;
    }
}
