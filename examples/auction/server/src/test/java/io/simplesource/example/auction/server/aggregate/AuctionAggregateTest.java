package io.simplesource.example.auction.server.aggregate;

import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Sequence;
import io.simplesource.example.auction.auction.wire.AuctionStatus;
import io.simplesource.example.auction.avro.AuctionAvroMappers;
import io.simplesource.example.auction.command.AuctionCommand;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.*;
import io.simplesource.example.auction.event.AuctionEvent;
import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.dsl.KafkaConfig;
import io.simplesource.kafka.testutils.AggregateTestDriver;
import io.simplesource.kafka.testutils.AggregateTestHelper;
import io.simplesource.kafka.util.PrefixResourceNamingStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class AuctionAggregateTest {

    private AggregateTestDriver<AuctionKey, AuctionCommand, AuctionEvent, Optional<Auction>> testAPI;
    private AggregateTestHelper<AuctionKey, AuctionCommand, AuctionEvent, Optional<Auction>> testHelper;

    @BeforeEach
    public void setup() {
        final AggregateSerdes<AuctionKey, AuctionCommand, AuctionEvent, Optional<Auction>> avroAggregateSerdes =
                new AuctionAvroMappers("http://mock-registry:8081", true).createAggregateSerdes();

        testAPI = new AggregateTestDriver<>(
                AuctionAggregate.createSpec(
                        "auction",
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
    public void testAuctionFlow() {
        final AuctionKey key = new AuctionKey(UUID.randomUUID());
        final String creator = "Bob";
        final String title = "Crayon";
        final String description = "Brand new, never used";
        final Money reservePrice = Money.valueOf("1");
        final Duration duration = Duration.ofDays(1);

        final Instant start = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        final ReservationId reservationId = new ReservationId(UUID.randomUUID());
        final AccountKey bidder = new AccountKey(UUID.randomUUID());
        final Money bidAmount = Money.valueOf("3");
        final Instant bidTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        testHelper.publishCommand(
                key,
                Sequence.first(),
                new AuctionCommand.CreateAuction(creator, title, description, reservePrice, duration))
                .expecting(
                        NonEmptyList.of(new AuctionEvent.AuctionCreated(creator, title, description, reservePrice, duration)),
                        Optional.of(new Auction(creator, title, description, reservePrice, reservePrice,
                                duration, AuctionStatus.CREATED, null, null, Collections.emptyList())))
                .thenPublish(
                        new AuctionCommand.StartAuction(start))
                .expecting(
                        NonEmptyList.of(new AuctionEvent.AuctionStarted(start)),
                        Optional.of(new Auction(creator, title, description, reservePrice, reservePrice,
                                duration, AuctionStatus.STARTED, start, null, Collections.emptyList())))
                .thenPublish(
                        new AuctionCommand.PlaceBid(reservationId, bidTime, bidder, bidAmount))
                .expecting(
                        NonEmptyList.of(new AuctionEvent.BidPlaced(reservationId, bidTime, bidder, bidAmount)),
                        Optional.of(new Auction(creator, title, description, reservePrice, bidAmount,
                                duration, AuctionStatus.STARTED, start, null, Collections.singletonList(new Bid(reservationId, bidTime, bidder, bidAmount)))))
        ;
    }
}
