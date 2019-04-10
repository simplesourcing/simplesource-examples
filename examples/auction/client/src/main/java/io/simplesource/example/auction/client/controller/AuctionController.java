package io.simplesource.example.auction.client.controller;

import io.simplesource.data.FutureResult;
import io.simplesource.data.Sequence;
import io.simplesource.example.auction.auction.wire.AuctionStatus;
import io.simplesource.example.auction.client.dto.AuctionDto;
import io.simplesource.example.auction.client.dto.BidDto;
import io.simplesource.example.auction.client.dto.CreateAuctionDto;
import io.simplesource.example.auction.client.service.AuctionWriteService;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.*;
import io.simplesource.saga.model.messages.SagaResponse;
import io.simplesource.saga.model.saga.SagaError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping(value = "/auction-example/auctions")
public final class AuctionController extends BaseController {

    @Autowired
    private AuctionWriteService auctionWriteService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity createAuction(@Valid @RequestBody CreateAuctionDto createAuctionDto) {
        Auction auction = toDomainAuction(createAuctionDto.getAuctionDto());
        FutureResult<AuctionError, Sequence> result = auctionWriteService.createAuction(toAuctionKey(createAuctionDto.getAuctionId()), auction);
        return toResponseEntity(result);
    }

    @RequestMapping(value = "/{auctionId}/start", method = RequestMethod.POST)
    public ResponseEntity startAuction(@NotNull @PathVariable UUID auctionId) {
        FutureResult<AuctionError, Sequence> result = auctionWriteService.startAuction(toAuctionKey(auctionId));

        return toResponseEntity(result);
    }

    @RequestMapping(value = "/{auctionId}/complete", method = RequestMethod.POST)
    public ResponseEntity completeAuction(@NotNull @PathVariable UUID auctionId) {
        FutureResult<AuctionError, SagaResponse> result = auctionWriteService.completeAuction(toAuctionKey(auctionId));

        return toSagaResponseEntity(result);
    }

    @RequestMapping(value = "/{auctionId}/bid", method = RequestMethod.POST)
    public ResponseEntity placeBid(@NotNull @PathVariable UUID auctionId, @Valid @RequestBody BidDto bidDto) {
        FutureResult<AuctionError, SagaResponse> result = auctionWriteService.placeBid(toAuctionKey(auctionId), toBid(bidDto));

        return toSagaResponseEntity(result);
    }

    private Bid toBid(BidDto bidDto) {
        return new Bid(
                ReservationId.of(bidDto.getReservationId().toString()),
                Instant.now(),
                AccountKey.of(bidDto.getAccountId().toString()),
                Money.valueOf(bidDto.getAmount()));
    }

    protected Auction toDomainAuction(AuctionDto auctionDto) {
        BigDecimal reservePrice = auctionDto.getReservePrice();
        return Auction.builder()
                .creator(auctionDto.getCreator())
                .title(auctionDto.getTitle())
                .description(auctionDto.getDescription())
                .reservePrice(Money.valueOf(reservePrice))
                .duration(Duration.ofMillis(auctionDto.getDuration()))
                .status(AuctionStatus.CREATED)
                .build();
    }
}
