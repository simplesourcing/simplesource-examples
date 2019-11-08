package io.simplesource.example.auction.client.service;

import io.simplesource.api.CommandAPI;
import io.simplesource.api.CommandError;
import io.simplesource.api.CommandId;
import io.simplesource.data.*;
import io.simplesource.example.auction.AppShared;
import io.simplesource.example.auction.account.wire.AccountSagaCommand;
import io.simplesource.example.auction.account.wire.CancelReservation;
import io.simplesource.example.auction.account.wire.CommitReservation;
import io.simplesource.example.auction.account.wire.ReserveFunds;
import io.simplesource.example.auction.auction.wire.AuctionSagaCommand;
import io.simplesource.example.auction.auction.wire.CompleteAuction;
import io.simplesource.example.auction.auction.wire.PlaceBid;
import io.simplesource.example.auction.client.repository.AccountRepository;
import io.simplesource.example.auction.client.repository.AuctionRepository;
import io.simplesource.example.auction.client.views.AccountView;
import io.simplesource.example.auction.client.views.AuctionView;
import io.simplesource.example.auction.client.views.BidView;
import io.simplesource.example.auction.command.AuctionCommand;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.Auction;
import io.simplesource.example.auction.domain.AuctionError;
import io.simplesource.example.auction.domain.AuctionKey;
import io.simplesource.example.auction.domain.Bid;
import io.simplesource.saga.client.dsl.SagaDSL;
import io.simplesource.saga.model.action.ActionId;
import io.simplesource.saga.model.api.SagaAPI;
import io.simplesource.saga.model.messages.SagaRequest;
import io.simplesource.saga.model.messages.SagaResponse;
import io.simplesource.saga.model.saga.SagaError;
import io.simplesource.saga.model.saga.SagaId;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@ExtensionMethod(OptionalExtension.class)
public class AuctionWriteServiceImpl implements AuctionWriteService {

    private Logger logger = LoggerFactory.getLogger(AuctionWriteServiceImpl.class);

    private static final Function<CommandError, AuctionError> COMMAND_ERROR_TO_AUCTION_ERROR_FUNCTION =
            ce -> new AuctionError.CommandError(ce);

    private final CommandAPI<AuctionKey, AuctionCommand> auctionCommandAPI;
    private final SagaAPI<GenericRecord> sagaAPI;
    private final AccountRepository accountRepository;
    private final AuctionRepository auctionRepository;
    private final Duration sagaResponseTimeout = Duration.ofMillis(10000);

    public AuctionWriteServiceImpl(
            CommandAPI<AuctionKey, AuctionCommand> auctionCommandAPI,
            SagaAPI<GenericRecord> sagaAPI,
            AccountRepository accountRepository,
            AuctionRepository auctionRepository) {
        this.auctionCommandAPI = auctionCommandAPI;
        this.sagaAPI = sagaAPI;
        this.accountRepository = accountRepository;
        this.auctionRepository = auctionRepository;
    }

    @Override
    public FutureResult<AuctionError, Sequence> createAuction(AuctionKey auctionKey, Auction auction) {
        requireNonNull(auction);
        requireNonNull(auctionKey);

        Optional<AuctionView> existingAuction = auctionRepository
                .findById(auctionKey.id().toString());

        List<AuctionError> validationErrorReasons =
                Stream.of(
                        validateRequired(auction.creator(), "Creator can not be empty"),
                        validateRequired(auction.title(), "Title can not be empty"),
                        validateRequired(auction.description(), "Description can not be empty"),
                        validateAmount(auction.reservePrice(), "Reserved price can not be negative"),
                        existingAuction.map(a -> new AuctionError.AuctionIdAlreadyExist(String.format("Auction ID %s already exist", a.getId())))
                )
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        return NonEmptyList.fromList(validationErrorReasons)
                .map(FutureResult::<AuctionError, Sequence>fail)
                .orElseGet(() -> {
                    logger.info("Creating auction {}", auction.title());
                    AuctionCommand.CreateAuction command = new AuctionCommand.CreateAuction(
                            auction.creator(),
                            auction.title(),
                            auction.description(),
                            auction.reservePrice(),
                            auction.duration());
                    return this.commandAndQueryAuction(auctionKey, Sequence.first(), command, Duration.ofMinutes(1));
                });
    }

    @Override
    public FutureResult<AuctionError, Sequence> startAuction(AuctionKey auctionKey) {
        requireNonNull(auctionKey);

        return this.commandAndQueryExistingAuction(auctionKey, new AuctionCommand.StartAuction(Instant.now()), Duration.ofMinutes(1));
    }

    @Override
    public FutureResult<AuctionError, SagaResponse> placeBid(AuctionKey auctionKey, Bid bid) {
        requireNonNull(auctionKey);
        requireNonNull(bid);

        // TODO Set the sequence numbers to be based on the existing auction...
        // - Does this need to be done for the action and the account?
        // - What needs to be done about the undo action?
        Optional<AuctionView> existingAuction = auctionRepository
                .findById(auctionKey.id().toString());

        Optional<AccountView> existingAccount = accountRepository
                .findById(bid.bidder().asString());

        Function<Pair<AuctionView, AccountView>, FutureResult<AuctionError, SagaResponse>> f =
            (pair) -> {
                val auction = pair._1();
                val account = pair._2();
                String desc = "Bid " + bid.amount().toString() + " on " + auction.getTitle();
                SagaDSL.SagaBuilder<GenericRecord> sagaBuilder = SagaDSL.SagaBuilder.create();
                sagaBuilder.addAction(
                        ActionId.random(),
                        AppShared.ACCOUNT_AGGREGATE_NAME,
                        new AccountSagaCommand(bid.bidder().asString(), new ReserveFunds(
                                bid.reservationId().asString(), bid.timestamp().toEpochMilli(), auctionKey.asString(), desc, bid.amount().getAmount()), account.getLastEventSequence()),
                        //// cancel (undo) the reservation if a bid cannot be placed
                        new AccountSagaCommand(bid.bidder().asString(), new CancelReservation(
                                bid.reservationId().asString()), account.getLastEventSequence()) // TODO Should this be incremented? Conditionally?
                ).andThen(sagaBuilder.addAction(
                        ActionId.random(),
                        AppShared.AUCTION_AGGREGATE_NAME,
                        new AuctionSagaCommand(auctionKey.asString(), new PlaceBid(
                                bid.reservationId().asString(), bid.timestamp().toEpochMilli(), bid.bidder().asString(), bid.amount().getAmount()), auction.getLastEventSequence())
                ));
                return sagaBuilder.build().fold(
                        e -> FutureResult.<AuctionError, SagaResponse>fail(new AuctionError.UnknownError()),
                        s -> {
                            FutureResult<SagaError, SagaId> response = sagaAPI.submitSaga(SagaRequest.<GenericRecord>of(SagaId.random(), s));
                            return response
                                    .flatMap(u -> sagaAPI.getSagaResponse(u, sagaResponseTimeout))
                                    .errorMap(e -> new AuctionError.CommandError());
                        }
                );
            };

        return existingAuction
                .zip(existingAccount)
                .fold(
                        () -> FutureResult.<AuctionError, SagaResponse>fail(new AuctionError.AuctionDoesNotExist()),
                        f
                );
    }

    @Override
    public FutureResult<AuctionError, SagaResponse> completeAuction(AuctionKey auctionKey) {
        requireNonNull(auctionKey);

        Optional<AuctionView> existingAuction = auctionRepository
                .findById(auctionKey.id().toString());

        Optional<FutureResult<AuctionError, SagaResponse>> result = existingAuction.map(auction -> {
            SagaDSL.SagaBuilder<GenericRecord> sagaBuilder = SagaDSL.SagaBuilder.create();
            if (auction.getBids().isEmpty()) {
                sagaBuilder.addAction(
                        ActionId.random(),
                        AppShared.AUCTION_AGGREGATE_NAME,
                        new AuctionSagaCommand(auctionKey.asString(), new CompleteAuction(), auction.getLastEventSequence())
                );
            } else {
                int numBids = auction.getBids().size();
                BidView winningBid = auction.getBids().get(numBids - 1);
                List<BidView> losingBids = auction.getBids().stream().limit(numBids - 1).collect(Collectors.toList());
                Optional<AccountView> winningAccount = accountRepository.findById(winningBid.getBidder().toString());
                List<AccountView> losingAccounts = accountRepository.findByAccountIds(losingBids.stream().map(bid -> bid.getBidder()).collect(Collectors.toList()));
                sagaBuilder.addAction(
                        ActionId.random(),
                        AppShared.AUCTION_AGGREGATE_NAME,
                        new AuctionSagaCommand(auctionKey.asString(), new CompleteAuction(), auction.getLastEventSequence())
                ).andThen(sagaBuilder.addAction(
                        ActionId.random(),
                        AppShared.ACCOUNT_AGGREGATE_NAME,
                        new AccountSagaCommand(winningBid.getBidder(), new CommitReservation(
                                winningBid.getReservationId(), winningBid.getAmount()), winningAccount.<Long>map(account -> account.getLastEventSequence()).orElse(0L))
                ).andThen(
                        SagaDSL.inParallel((List<SagaDSL.SubSaga<GenericRecord>>) losingBids.stream().<SagaDSL.SubSaga<GenericRecord>>map(b -> {
                                //
                                long sequence = losingAccounts.stream().filter(c -> c.getId().equals(b.getBidder())).findFirst().map(c -> c.getLastEventSequence()).orElse(0L);
                                return sagaBuilder.addAction(ActionId.random(),
                                        AppShared.ACCOUNT_AGGREGATE_NAME,
                                        new AccountSagaCommand(b.getBidder(), new CancelReservation(
                                                b.getReservationId()), sequence)
                                );
                            }
                        ).collect(Collectors.toList()))
                ));
            }
            return sagaBuilder.build().fold(
                    e -> FutureResult.<AuctionError, SagaResponse>fail(new AuctionError.UnknownError()),
                    s -> {
                      FutureResult<SagaError, SagaId> response = sagaAPI.submitSaga(SagaRequest.<GenericRecord>of(SagaId.random(), s));
                      return response
                            .flatMap(u -> sagaAPI.getSagaResponse(u, Duration.ofMillis(10000)))
                            .errorMap(e -> new AuctionError.CommandError());
                    }
            );

        });
        return result.orElse(FutureResult.fail(new AuctionError.AuctionDoesNotExist()));
    }

    private Optional<AuctionError> validateRequired(String creator, String message) {
        return StringUtils.isEmpty(creator) ?
                Optional.of(new AuctionError.InvalidData(message)) :
                Optional.empty();
    }

    private Optional<AuctionError> validateAmount(Money amount, String message) {
        return (amount == null || amount.isNegativeAmount()) ?
                Optional.of(new AuctionError.InvalidData(message)) :
                Optional.empty();
    }

    private <C extends AuctionCommand> FutureResult<AuctionError, Sequence> commandAndQueryAuction(AuctionKey auctionKey,
                                                                                                   Sequence sequence, C command,
                                                                                                   Duration duration) {

        FutureResult<CommandError, Sequence> commandResult = auctionCommandAPI.publishAndQueryCommand(new CommandAPI.Request<>(CommandId.random(),
                auctionKey, sequence, command), duration);

        Function<NonEmptyList<CommandError>, Result<AuctionError, Sequence>> failureMapFunc =
                ers -> Result.failure(ers.map(COMMAND_ERROR_TO_AUCTION_ERROR_FUNCTION));

        Future<Result<AuctionError, Sequence>> future = commandResult.fold(failureMapFunc, Result::success);

        return FutureResult.ofFutureResult(future, AuctionError.UnknownError::new);
    }

    private <C extends AuctionCommand> FutureResult<AuctionError, Sequence> commandAndQueryExistingAuction(AuctionKey auctionKey,
                                                                                                           C command,
                                                                                                           Duration duration) {
        Optional<AuctionView> maybeAuction = auctionRepository.findById(auctionKey.asString());

        return maybeAuction
                .map(a -> commandAndQueryAuction(auctionKey, Sequence.position(a.getLastEventSequence()), command, duration))
                .orElse(FutureResult.fail(new AuctionError.AuctionDoesNotExist()));
    }
}
