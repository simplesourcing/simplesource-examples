package io.simplesource.example.auction.server.command;

import io.simplesource.api.CommandError;
import io.simplesource.api.CommandHandler;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Result;
import io.simplesource.example.auction.auction.wire.AuctionStatus;
import io.simplesource.example.auction.command.AuctionCommand;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.*;
import io.simplesource.example.auction.event.AuctionEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AuctionCommandHandlerTest {
    private CommandHandler<AuctionKey, AuctionCommand, AuctionEvent, Optional<Auction>> handler =
            AuctionCommandHandler.instance;

    private AuctionKey key = new AuctionKey(UUID.randomUUID());

    @Test
    public void createAuctionSuccess() {
        Result<CommandError, NonEmptyList<AuctionEvent>> result =
                handler.interpretCommand(key, Optional.empty(), new AuctionCommand.CreateAuction(
                        "creator", "title", "desc", Money.valueOf("1"), Duration.ofDays(1)));
        assertThat(result).isEqualTo(Result.success(NonEmptyList.of(new AuctionEvent.AuctionCreated(
                "creator", "title", "desc", Money.valueOf("1"), Duration.ofDays(1)))));
    }

    @Test
    public void createAuctionFailsIfAlreadyExists() {
        Auction auction = new Auction("creator", "title", "desc", Money.valueOf("1"), null,
                Duration.ofDays(1), AuctionStatus.CREATED, null, null, Collections.emptyList());
        Result<CommandError, NonEmptyList<AuctionEvent>> result =
                handler.interpretCommand(key, Optional.of(auction), new AuctionCommand.CreateAuction(
                        "creator", "title", "desc", Money.valueOf("1"), Duration.ofDays(1)));
        assertThat(result).isEqualTo(Result.failure(NonEmptyList.of(CommandError.of(CommandError.Reason.InvalidCommand,
                "Auction already created: " + key.asString()))));
    }

    @Test
    public void startAuctionSuccess() {
        Instant start = Instant.now();
        Auction auction = new Auction("creator", "title", "desc", Money.valueOf("1"), null,
                Duration.ofDays(1), AuctionStatus.CREATED, null, null, Collections.emptyList());
        Result<CommandError, NonEmptyList<AuctionEvent>> result =
                handler.interpretCommand(key, Optional.of(auction), new AuctionCommand.StartAuction(
                        start));
        assertThat(result).isEqualTo(Result.success(NonEmptyList.of(new AuctionEvent.AuctionStarted(
                start))));
    }

    @Test
    public void startAuctionFailsIfAlreadyStarted() {
        Instant start = Instant.now();
        Auction auction = new Auction("creator", "title", "desc", Money.valueOf("1"), null,
                Duration.ofDays(1), AuctionStatus.STARTED, start, null, Collections.emptyList());
        Result<CommandError, NonEmptyList<AuctionEvent>> result =
                handler.interpretCommand(key, Optional.of(auction), new AuctionCommand.StartAuction(
                        start));
        assertThat(result).isEqualTo(Result.failure(NonEmptyList.of(CommandError.of(CommandError.Reason.InvalidCommand,
                "Cannot start an auction in STARTED state"))));
    }

    @Test
    public void placeBidSuccess() {
        Instant start = Instant.now();
        ReservationId reservationId = new ReservationId(UUID.randomUUID());
        AccountKey bidder = new AccountKey(UUID.randomUUID());
        Money amount = Money.valueOf("2");
        Instant timestamp = Instant.now();
        Auction auction = new Auction("creator", "title", "desc", Money.valueOf("1"), null,
                Duration.ofDays(1), AuctionStatus.STARTED, start, null, Collections.emptyList());
        Result<CommandError, NonEmptyList<AuctionEvent>> result =
                handler.interpretCommand(key, Optional.of(auction), new AuctionCommand.PlaceBid(
                        reservationId, timestamp, bidder, amount));
        assertThat(result).isEqualTo(Result.success(NonEmptyList.of(new AuctionEvent.BidPlaced(
                reservationId, timestamp, bidder, amount))));
    }

    @Test
    public void placeBidFailsIfAuctionDoesNotExist() {
        ReservationId reservationId = new ReservationId(UUID.randomUUID());
        AccountKey bidder = new AccountKey(UUID.randomUUID());
        Money amount = Money.valueOf("2");
        Result<CommandError, NonEmptyList<AuctionEvent>> result =
                handler.interpretCommand(key, Optional.empty(), new AuctionCommand.PlaceBid(
                        reservationId, Instant.now(), bidder, amount));
        assertThat(result).isEqualTo(Result.failure(NonEmptyList.of(CommandError.of(CommandError.Reason.InvalidCommand,
                "Auction does not exist"))));
    }

    @Test
    public void placeBidSucceedsIfHigherThanPreviousBid() {
        Instant start = Instant.now();
        ReservationId reservationId = new ReservationId(UUID.randomUUID());
        AccountKey bidder = new AccountKey(UUID.randomUUID());
        Money amount = Money.valueOf("3");
        Instant timestamp = Instant.now();
        Auction auction = new Auction("creator", "title", "desc", Money.valueOf("1"), null,
                Duration.ofDays(1), AuctionStatus.STARTED, start, null, Collections.singletonList(
                new Bid(new ReservationId(UUID.randomUUID()), timestamp, new AccountKey(UUID.randomUUID()), Money.valueOf("2"))));
        Result<CommandError, NonEmptyList<AuctionEvent>> result =
                handler.interpretCommand(key, Optional.of(auction), new AuctionCommand.PlaceBid(
                        reservationId, timestamp, bidder, amount));
        assertThat(result).isEqualTo(Result.success(NonEmptyList.of(new AuctionEvent.BidPlaced(
                reservationId, timestamp, bidder, amount))));
    }

    @Test
    public void placeBidFailsIfBidEqualsPreviousBid() {
        Instant start = Instant.now();
        ReservationId reservationId = new ReservationId(UUID.randomUUID());
        AccountKey bidder = new AccountKey(UUID.randomUUID());
        Money amount = Money.valueOf("2");
        Instant timestamp = Instant.now();
        Auction auction = new Auction("creator", "title", "desc", Money.valueOf("1"), null,
                Duration.ofDays(1), AuctionStatus.STARTED, start, null, Collections.singletonList(
                        new Bid(new ReservationId(UUID.randomUUID()), timestamp, new AccountKey(UUID.randomUUID()), amount)));
        Result<CommandError, NonEmptyList<AuctionEvent>> result =
                handler.interpretCommand(key, Optional.of(auction), new AuctionCommand.PlaceBid(
                        reservationId, timestamp, bidder, amount));
        assertThat(result).isEqualTo(Result.failure(NonEmptyList.of(CommandError.of(CommandError.Reason.InvalidCommand,
                "Bid must exceed existing high bid"))));
    }

    @Test
    public void placeBidFailsIfBidLowerThanPreviousBid() {
        Instant start = Instant.now();
        ReservationId reservationId = new ReservationId(UUID.randomUUID());
        AccountKey bidder = new AccountKey(UUID.randomUUID());
        Money amount = Money.valueOf("2");
        Instant timestamp = Instant.now();
        Auction auction = new Auction("creator", "title", "desc", Money.valueOf("1"), null,
                Duration.ofDays(1), AuctionStatus.STARTED, start, null, Collections.singletonList(
                        new Bid(new ReservationId(UUID.randomUUID()), timestamp, new AccountKey(UUID.randomUUID()), amount)));
        Result<CommandError, NonEmptyList<AuctionEvent>> result =
                handler.interpretCommand(key, Optional.of(auction), new AuctionCommand.PlaceBid(
                        reservationId, timestamp, bidder, amount));
        assertThat(result).isEqualTo(Result.failure(NonEmptyList.of(CommandError.of(CommandError.Reason.InvalidCommand,
                "Bid must exceed existing high bid"))));
    }

    @Test
    public void placeBidFailsIfBidLowerThanReservePrice() {
        Instant start = Instant.now();
        ReservationId reservationId = new ReservationId(UUID.randomUUID());
        AccountKey bidder = new AccountKey(UUID.randomUUID());
        Money amount = Money.valueOf("1");
        Instant timestamp = Instant.now();
        Auction auction = new Auction("creator", "title", "desc", Money.valueOf("2"), null,
                Duration.ofDays(1), AuctionStatus.STARTED, start, null, Collections.emptyList());
        Result<CommandError, NonEmptyList<AuctionEvent>> result =
                handler.interpretCommand(key, Optional.of(auction), new AuctionCommand.PlaceBid(
                        reservationId, timestamp, bidder, amount));
        assertThat(result).isEqualTo(Result.failure(NonEmptyList.of(CommandError.of(CommandError.Reason.InvalidCommand,
                "Bid must match or exceed reserve price"))));
    }

    @Test
    public void placeBidFailsIfAuctionNotYetStarted() {
        ReservationId reservationId = new ReservationId(UUID.randomUUID());
        AccountKey bidder = new AccountKey(UUID.randomUUID());
        Money amount = Money.valueOf("2");
        Instant timestamp = Instant.now();
        Auction auction = new Auction("creator", "title", "desc", Money.valueOf("1"), null,
                Duration.ofDays(1), AuctionStatus.CREATED, null, null, Collections.singletonList(
                        new Bid(new ReservationId(UUID.randomUUID()), timestamp, new AccountKey(UUID.randomUUID()), amount)));
        Result<CommandError, NonEmptyList<AuctionEvent>> result =
                handler.interpretCommand(key, Optional.of(auction), new AuctionCommand.PlaceBid(
                        reservationId, timestamp, bidder, amount));
        assertThat(result).isEqualTo(Result.failure(NonEmptyList.of(CommandError.of(CommandError.Reason.InvalidCommand,
                "Cannot place bid on auction in CREATED state"))));
    }

    @Test
    public void placeBidFailsIfAuctionAlreadyCompleted() {
        ReservationId reservationId = new ReservationId(UUID.randomUUID());
        AccountKey bidder = new AccountKey(UUID.randomUUID());
        Money amount = Money.valueOf("2");
        Instant timestamp = Instant.now();
        Auction auction = new Auction("creator", "title", "desc", Money.valueOf("1"), null,
                Duration.ofDays(1), AuctionStatus.COMPLETED, null, null, Collections.singletonList(
                        new Bid(new ReservationId(UUID.randomUUID()), timestamp, new AccountKey(UUID.randomUUID()), amount)));
        Result<CommandError, NonEmptyList<AuctionEvent>> result =
                handler.interpretCommand(key, Optional.of(auction), new AuctionCommand.PlaceBid(
                        reservationId, timestamp, bidder, amount));
        assertThat(result).isEqualTo(Result.failure(NonEmptyList.of(CommandError.of(CommandError.Reason.InvalidCommand,
                "Cannot place bid on auction in COMPLETED state"))));
    }

    @Test
    public void placeBidFailsIfPassedEndTime() {
        Instant start = Instant.now().minus(Duration.ofDays(2));
        ReservationId reservationId = new ReservationId(UUID.randomUUID());
        AccountKey bidder = new AccountKey(UUID.randomUUID());
        Money amount = Money.valueOf("2");
        Instant timestamp = Instant.now();
        Auction auction = new Auction("creator", "title", "desc", Money.valueOf("1"), null,
                Duration.ofDays(1), AuctionStatus.STARTED, start, null, Collections.singletonList(
                        new Bid(new ReservationId(UUID.randomUUID()), timestamp, new AccountKey(UUID.randomUUID()), amount)));
        Result<CommandError, NonEmptyList<AuctionEvent>> result =
                handler.interpretCommand(key, Optional.of(auction), new AuctionCommand.PlaceBid(
                        reservationId, timestamp, bidder, amount));
        assertThat(result).isEqualTo(Result.failure(NonEmptyList.of(CommandError.of(CommandError.Reason.InvalidCommand,
                "Auction has ended"))));
    }

    @Test
    public void completeAuctionSuccess() {
        Money amount = Money.valueOf("2");
        Instant start = Instant.now().minusSeconds(120);
        Auction auction = new Auction("creator", "title", "desc", Money.valueOf("1"), null,
                Duration.ofSeconds(60), AuctionStatus.STARTED, start, null, Collections.singletonList(
                new Bid(new ReservationId(UUID.randomUUID()), Instant.now(), new AccountKey(UUID.randomUUID()), amount)));
        Result<CommandError, NonEmptyList<AuctionEvent>> result =
                handler.interpretCommand(key, Optional.of(auction), new AuctionCommand.CompleteAuction());
        assertThat(result).isEqualTo(Result.success(NonEmptyList.of(new AuctionEvent.AuctionCompleted())));
    }

    @Test
    public void completeAuctionFailsIfNotInStartedState() {
        Money amount = Money.valueOf("2");
        Instant start = Instant.now().minusSeconds(120);
        Auction auction = new Auction("creator", "title", "desc", Money.valueOf("1"), null,
                Duration.ofSeconds(60), AuctionStatus.COMPLETED, start, null, Collections.singletonList(
                new Bid(new ReservationId(UUID.randomUUID()), Instant.now(), new AccountKey(UUID.randomUUID()), amount)));
        Result<CommandError, NonEmptyList<AuctionEvent>> result =
                handler.interpretCommand(key, Optional.of(auction), new AuctionCommand.CompleteAuction());
        assertThat(result).isEqualTo(Result.failure(NonEmptyList.of(CommandError.of(CommandError.Reason.InvalidCommand,
                "Cannot complete an auction in COMPLETED state"))));
    }

    @Test
    public void completeAuctionFailsIfNotPassedEndTime() {
        Money amount = Money.valueOf("2");
        Instant start = Instant.now();
        Auction auction = new Auction("creator", "title", "desc", Money.valueOf("1"), null,
                Duration.ofDays(1), AuctionStatus.STARTED, start, null, Collections.singletonList(
                new Bid(new ReservationId(UUID.randomUUID()), Instant.now(), new AccountKey(UUID.randomUUID()), amount)));
        Result<CommandError, NonEmptyList<AuctionEvent>> result =
                handler.interpretCommand(key, Optional.of(auction), new AuctionCommand.CompleteAuction());
        assertThat(result).isEqualTo(Result.failure(NonEmptyList.of(CommandError.of(CommandError.Reason.InvalidCommand,
                "Auction has not reached the allotted time"))));
    }
}
