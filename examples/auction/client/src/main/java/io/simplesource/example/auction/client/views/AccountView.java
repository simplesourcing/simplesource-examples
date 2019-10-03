package io.simplesource.example.auction.client.views;

import io.simplesource.example.auction.domain.Reservation;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;


import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Document(collection = "auction_account")
public class AccountView {

    @Id
    private final String id;

    @Field("value.username")
    private final String userName;

    @Field("value.funds")
    private final BigDecimal funds = BigDecimal.ZERO;

    private final BigDecimal availableFunds;

    @Field("sequence")
    private final long lastEventSequence;

    @Field("value.reservations")
    private final List<ReservationView> draftReservations;


    public AccountView(String id, String userName, BigDecimal availableFunds, long lastEventSequence, List<ReservationView> draftReservations) {
        this.id = id;
        this.userName = userName;
        this.availableFunds = availableFunds;
        this.lastEventSequence = lastEventSequence;
        this.draftReservations = Collections.unmodifiableList(draftReservations);
    }

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
