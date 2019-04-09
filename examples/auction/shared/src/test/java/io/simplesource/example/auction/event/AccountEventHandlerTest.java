package io.simplesource.example.auction.event;

import io.simplesource.api.Aggregator;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.Account;
import io.simplesource.example.auction.domain.AuctionKey;
import io.simplesource.example.auction.domain.Reservation;
import io.simplesource.example.auction.domain.ReservationId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountEventHandlerTest {

    private Aggregator<AccountEvent, Optional<Account>> handler = AccountEventHandler.instance;

    private Account account = new Account("Bob", Money.valueOf("5"), Collections.emptyList());
    private ReservationId reservationId = new ReservationId(UUID.randomUUID());
    private AuctionKey auction = new AuctionKey(UUID.randomUUID());
    private Instant timestamp = Instant.now();
    private Reservation reservation = new Reservation(reservationId, timestamp, auction, Money.valueOf("2"), "Desc", Reservation.Status.DRAFT);
    private Account accountWithReservation = new Account("Bob", Money.valueOf("5"), Collections.singletonList(reservation));

    @Test
    public void testAccountCreated() {
        assertThat(handler.applyEvent(Optional.empty(), new AccountEvent.AccountCreated("Bob", Money.valueOf("5"))))
                .isEqualTo(Optional.of(account));
    }

    @Test
    public void testAccountUpdated() {
        assertThat(handler.applyEvent(Optional.of(account), new AccountEvent.AccountCreated("Alice", Money.valueOf("2"))))
                .isEqualTo(Optional.of(new Account("Alice", Money.valueOf("2"), Collections.emptyList())));
    }

    @Test
    public void testFundsUpdated() {
        assertThat(handler.applyEvent(Optional.of(account), new AccountEvent.FundsAdded(Money.valueOf("2"))))
                .isEqualTo(Optional.of(new Account("Bob", Money.valueOf("7"), Collections.emptyList())));
    }

    @Test
    public void testFundsReserved() {
        assertThat(handler.applyEvent(Optional.of(account), new AccountEvent.FundsReserved(reservationId, timestamp, auction, Money.valueOf("2"), "Desc")))
                .isEqualTo(Optional.of(accountWithReservation));
    }

    @Test
    public void testFundsReservedForIncreasedBidOnSameAuction() {
        ReservationId reservation2 = new ReservationId(UUID.randomUUID());
        assertThat(handler.applyEvent(Optional.of(accountWithReservation), new AccountEvent.FundsReserved(reservation2, timestamp, auction, Money.valueOf("4"), "Increase")))
                .isEqualTo(Optional.of(new Account("Bob", Money.valueOf("5"),
                        Collections.singletonList(new Reservation(reservation2, timestamp, auction, Money.valueOf("4"), "Increase", Reservation.Status.DRAFT)))));
    }

    @Test
    public void testFundsReservedForAnotherAuction() {
        ReservationId reservation2 = new ReservationId(UUID.randomUUID());
        AuctionKey auction2 = new AuctionKey(UUID.randomUUID());
        assertThat(handler.applyEvent(Optional.of(accountWithReservation), new AccountEvent.FundsReserved(reservation2, timestamp, auction2, Money.valueOf("1"), "Desc2")))
                .isEqualTo(Optional.of(new Account("Bob", Money.valueOf("5"),
                        Arrays.asList(reservation, new Reservation(reservation2, timestamp, auction2, Money.valueOf("1"), "Desc2", Reservation.Status.DRAFT)))));
    }

    @Test
    public void testReservationConfirmed() {
        assertThat(handler.applyEvent(Optional.of(accountWithReservation), new AccountEvent.ReservationConfirmed(reservationId, Money.valueOf("2"))))
                .isEqualTo(Optional.of(new Account("Bob", Money.valueOf("3"),
                        Collections.emptyList())));
    }

    @Test
    public void testReservationCanceled() {

        assertThat(handler.applyEvent(Optional.of(accountWithReservation), new AccountEvent.FundsReservationCancelled(reservationId)))
                .isEqualTo(Optional.of(new Account("Bob", Money.valueOf("5"),
                        Collections.emptyList())));
    }
}
