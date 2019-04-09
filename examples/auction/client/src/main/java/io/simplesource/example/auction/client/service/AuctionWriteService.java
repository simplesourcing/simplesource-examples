package io.simplesource.example.auction.client.service;

import io.simplesource.data.FutureResult;
import io.simplesource.data.Sequence;
import io.simplesource.example.auction.domain.*;
import io.simplesource.saga.model.messages.SagaResponse;
import io.simplesource.saga.model.saga.SagaError;

/**
 * Write (command) service for auctions.
 */
public interface AuctionWriteService {

    /**
     * Create a new auction.
     * @param auctionKey auction identifier.
     * @param auction to create.
     * @return FutureResult with the command sequence number or validation error.
     */
    FutureResult<AuctionError, Sequence> createAuction(AuctionKey auctionKey, Auction auction);

    /**
     * Start an auction.
     * @param auctionKey auction to start.
     * @return FutureResult with the command sequence number or validation error.
     */
    FutureResult<AuctionError, Sequence> startAuction(AuctionKey auctionKey);

    /**
     * Place a bid on an auction. This is a saga because funds must be reserved from an account and a
     * bid placed on the auction in a single transaction. If the bid on the auction is outbid then the
     * reservation must be rolled back.
     *
     * @param auctionKey auction to start.
     * @param bid to place.
     * @return FutureResult with the command sequence number or validation error.
     */
    FutureResult<AuctionError, SagaResponse> placeBid(AuctionKey auctionKey, Bid bid);

    /**
     * Saga to complete an auction.
     * - The auction is updated to COMPLETED status and winner determined.
     * - Reservation for the winning account is confirmed.
     * - Reservations for all losing accounts are cancelled.
     * @param auctionKey auction to complete.
     * @return FutureResult with the command sequence number or validation error.
     */
    FutureResult<AuctionError, SagaResponse> completeAuction(AuctionKey auctionKey);
}
