package io.simplesource.example.auction.server.aggregate;

import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Sequence;
import io.simplesource.example.auction.avro.AccountAvroMappers;
import io.simplesource.example.auction.command.AccountCommand;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.*;
import io.simplesource.example.auction.event.AccountEvent;
import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.dsl.KafkaConfig;
import io.simplesource.kafka.testutils.AggregateTestDriver;
import io.simplesource.kafka.testutils.AggregateTestHelper;
import io.simplesource.kafka.util.PrefixResourceNamingStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class AccountAggregateTest {

    private AggregateTestDriver<AccountKey, AccountCommand, AccountEvent, Optional<Account>> testAPI;
    private AggregateTestHelper<AccountKey, AccountCommand, AccountEvent, Optional<Account>> testHelper;

    @BeforeEach
    public void setup() {
        final AggregateSerdes<AccountKey, AccountCommand, AccountEvent, Optional<Account>> avroAggregateSerdes =
                new AccountAvroMappers("http://mock-registry:8081", true).createAggregateSerdes();

        testAPI = new AggregateTestDriver<>(
                AccountAggregate.createSpec(
                        "account",
                        avroAggregateSerdes,
                        new PrefixResourceNamingStrategy("auction_mapped_avro_"),
                        k -> Optional.empty()
                ),
                new KafkaConfig.Builder()
                        .withKafkaApplicationId("testApp")
                        .withKafkaBootstrap("0.0.0.0:9092")
                        .withExactlyOnce()
                        .build());
        testHelper = new AggregateTestHelper<>(testAPI);
    }

    @AfterEach
    public void tearDown() {
        if (testAPI != null) {
            testAPI.close();
        }
    }

    @Test
    public void testReservationFlow() {
        final AccountKey key = new AccountKey(UUID.randomUUID());
        final String username = "Bob";
        final Money initialFunds = Money.valueOf("10");
        final ReservationId reservationId = new ReservationId(UUID.randomUUID());
        final AuctionKey auction = new AuctionKey(UUID.randomUUID());
        final Money reserveAmount = Money.valueOf("4");
        final String reserveDesc = "Reserve $4";
        final Instant timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        testHelper.publishCommand(
                key,
                Sequence.first(),
                new AccountCommand.CreateAccount(username, initialFunds))
                .expecting(
                        NonEmptyList.of(new AccountEvent.AccountCreated(username, initialFunds)),
                        Optional.of(new Account(username, initialFunds, Collections.emptyList())))
                .thenPublish(
                        new AccountCommand.ReserveFunds(reservationId, timestamp, auction, reserveAmount, reserveDesc))
                .expecting(
                        NonEmptyList.of(new AccountEvent.FundsReserved(reservationId, timestamp, auction, reserveAmount, reserveDesc)),
                        Optional.of(new Account(username, initialFunds, Arrays.asList(
                                new Reservation(reservationId, timestamp, auction, reserveAmount, reserveDesc, Reservation.Status.DRAFT)))))
                .thenPublish(
                        new AccountCommand.ConfirmReservation(reservationId, reserveAmount))
                .expecting(
                        NonEmptyList.of(new AccountEvent.ReservationConfirmed(reservationId, reserveAmount)),
                        Optional.of(new Account(username, Money.valueOf("6"), Collections.emptyList()))
                );
    }

    @Test
    public void testCancelReservationFlow() {
        final AccountKey key = new AccountKey(UUID.randomUUID());
        final String username = "Bob";
        final Money initialFunds = Money.valueOf("10");
        final ReservationId reservationId = new ReservationId(UUID.randomUUID());
        final AuctionKey auction = new AuctionKey(UUID.randomUUID());
        final Money reserveAmount = Money.valueOf("4");
        final String reserveDesc = "Reserve $4";
        final Instant timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        testHelper.publishCommand(
                key,
                Sequence.first(),
                new AccountCommand.CreateAccount(username, initialFunds))
                .expecting(
                        NonEmptyList.of(new AccountEvent.AccountCreated(username, initialFunds)),
                        Optional.of(new Account(username, initialFunds, Collections.emptyList())))
                .thenPublish(
                        new AccountCommand.ReserveFunds(reservationId, timestamp, auction, reserveAmount, reserveDesc))
                .expecting(
                        NonEmptyList.of(new AccountEvent.FundsReserved(reservationId, timestamp, auction, reserveAmount, reserveDesc)),
                        Optional.of(new Account(username, initialFunds, Arrays.asList(
                                new Reservation(reservationId, timestamp, auction, reserveAmount, reserveDesc, Reservation.Status.DRAFT)))))
                .thenPublish(
                        new AccountCommand.CancelReservation(reservationId))
                .expecting(
                        NonEmptyList.of(new AccountEvent.FundsReservationCancelled(reservationId)),
                        Optional.of(new Account(username, initialFunds, Collections.emptyList()))
                );
    }

    @Test
    public void testUpdateReservationFlow() {
        final AccountKey key = new AccountKey(UUID.randomUUID());
        final String username = "Bob";
        final Money initialFunds = Money.valueOf("10");
        final ReservationId reservationId = new ReservationId(UUID.randomUUID());
        final AuctionKey auction = new AuctionKey(UUID.randomUUID());
        final Money reserveAmount = Money.valueOf("4");
        final ReservationId updatedReservationId = new ReservationId(UUID.randomUUID());
        final String reserveDesc = "Reserve $4";
        final Money updatedAmount = Money.valueOf("6");
        final String updatedDesc = "Reserve $6";
        final Instant timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        testHelper.publishCommand(
                key,
                Sequence.first(),
                new AccountCommand.CreateAccount(username, initialFunds))
                .expecting(
                        NonEmptyList.of(new AccountEvent.AccountCreated(username, initialFunds)),
                        Optional.of(new Account(username, initialFunds, Collections.emptyList())))
                .thenPublish(
                        new AccountCommand.ReserveFunds(reservationId, timestamp, auction, reserveAmount, reserveDesc))
                .expecting(
                        NonEmptyList.of(new AccountEvent.FundsReserved(reservationId, timestamp, auction, reserveAmount, reserveDesc)),
                        Optional.of(new Account(username, initialFunds, Arrays.asList(
                                new Reservation(reservationId, timestamp, auction, reserveAmount, reserveDesc, Reservation.Status.DRAFT)))))
                .thenPublish(
                        new AccountCommand.ReserveFunds(updatedReservationId, timestamp, auction, updatedAmount, updatedDesc))
                .expecting(
                        NonEmptyList.of(new AccountEvent.FundsReserved(updatedReservationId, timestamp, auction, updatedAmount, updatedDesc)),
                        Optional.of(new Account(username, initialFunds, Arrays.asList(
                                new Reservation(updatedReservationId, timestamp, auction, updatedAmount, updatedDesc, Reservation.Status.DRAFT))))
                );
    }
}
