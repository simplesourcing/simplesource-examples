package io.simplesource.example.auction.server.command;

import io.simplesource.api.CommandError;
import io.simplesource.api.CommandHandler;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Result;
import io.simplesource.dsl.CommandHandlerBuilder;
import io.simplesource.example.auction.command.AuctionCommand;
import io.simplesource.example.auction.domain.Auction;
import io.simplesource.example.auction.domain.AuctionKey;
import io.simplesource.example.auction.event.AuctionEvent;

import java.time.Instant;
import java.util.Optional;

import static io.simplesource.data.NonEmptyList.of;
import static io.simplesource.example.auction.auction.wire.AuctionStatus.CREATED;
import static io.simplesource.example.auction.auction.wire.AuctionStatus.STARTED;

public final class AuctionCommandHandler {

    public static CommandHandler<AuctionKey, AuctionCommand, AuctionEvent, Optional<Auction>> instance =
            CommandHandlerBuilder.<AuctionKey, AuctionCommand, AuctionEvent, Optional<Auction>>newBuilder()
                    .onCommand(AuctionCommand.CreateAuction.class, doCreateAuction())
                    .onCommand(AuctionCommand.StartAuction.class, doStartAuction())
                    .onCommand(AuctionCommand.PlaceBid.class, doPlaceBid())
                    .onCommand(AuctionCommand.CompleteAuction.class, doCompleteAuction())
                    .build();

    private static CommandHandler<AuctionKey, AuctionCommand.CreateAuction, AuctionEvent, Optional<Auction>> doCreateAuction() {
        return (auctionId, currentAggregate, command) -> currentAggregate
                .map(d -> failure("Auction already created: " + auctionId.id()))
                .orElse(success(new AuctionEvent.AuctionCreated(command.creator(), command.title(), command.description(), command.reservePrice(), command.duration())));
    }

    private static CommandHandler<AuctionKey, AuctionCommand.StartAuction, AuctionEvent, Optional<Auction>> doStartAuction() {
        return ((auctionId, currentAggregate, command) -> currentAggregate
                .map(a -> a.status() == CREATED ?
                        success(new AuctionEvent.AuctionStarted(command.start())) :
                        failure("Cannot start an auction in " + a.status().toString() + " state"))
                .orElse(failure("Auction does not exist")));
    }

    private static CommandHandler<AuctionKey, AuctionCommand.PlaceBid, AuctionEvent, Optional<Auction>> doPlaceBid() {
        return ((auctionId, currentAggregate, command) -> currentAggregate
                .map(a -> {
                    if (a.status() == STARTED) {
                        if (Instant.now().isAfter(a.end())) {
                            return failure("Auction has ended");
                        } else if (a.winningBid().map(b -> b.amount().compareTo(command.amount()) >= 0).orElse(false)) {
                            return failure("Bid must exceed existing high bid");
                        } else if (a.reservePrice().compareTo(command.amount()) > 0) {
                            return failure("Bid must match or exceed reserve price");
                        } else {
                            return success(new AuctionEvent.BidPlaced(command.reservationId(), command.timestamp(), command.bidder(), command.amount()));
                        }
                    } else {
                        return failure("Cannot place bid on auction in " + a.status().toString() + " state");
                    }
                })
                .orElse(failure("Auction does not exist")));
    }

    private static CommandHandler<AuctionKey, AuctionCommand.CompleteAuction, AuctionEvent, Optional<Auction>> doCompleteAuction() {
        return ((auctionId, currentAggregate, command) -> currentAggregate
                .map(a -> {
                    if (a.status() != STARTED) {
                        return failure("Cannot complete an auction in " + a.status().toString() + " state");
                    } else if (Instant.now().isBefore(a.start().plus(a.duration()))) {
                        return failure("Auction has not reached the allotted time");
                    } else {
                        return success(new AuctionEvent.AuctionCompleted());
                    }
                })
                .orElse(failure("Auction does not exist")));
    }

    private static Result<CommandError, NonEmptyList<AuctionEvent>> failure(final String message) {
        return Result.failure(new CommandError.InvalidCommand(message));
    }

    private static Result<CommandError, NonEmptyList<AuctionEvent>> failure(final NonEmptyList<CommandError> reasons) {
        return Result.failure(reasons);
    }

    @SafeVarargs
    private static <Event extends AuctionEvent> Result<CommandError, NonEmptyList<AuctionEvent>> success(final Event event, final Event... events) {
        return Result.success(of(event, events));
    }
}
