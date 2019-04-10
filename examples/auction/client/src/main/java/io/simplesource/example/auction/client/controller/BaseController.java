package io.simplesource.example.auction.client.controller;

import io.simplesource.data.FutureResult;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Result;
import io.simplesource.example.auction.client.dto.SagaErrorDto;
import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.example.auction.domain.AuctionKey;
import io.simplesource.saga.model.messages.SagaResponse;
import io.simplesource.saga.model.saga.SagaError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

class BaseController {

    AccountKey toAccountKey(UUID accountId) {
        return new AccountKey(accountId);
    }

    AuctionKey toAuctionKey(UUID auctionId) {
        return new AuctionKey(auctionId);
    }

    protected <E, T> ResponseEntity toResponseEntity(FutureResult<E, T> futureResult) {
        Result<E, T> result = futureResult.future().join();
        return result.fold(
                e -> toErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e),
                r -> ResponseEntity.ok(r)
        );
    }

    protected <E> ResponseEntity toSagaResponseEntity(FutureResult<E, SagaResponse> futureResult) {
        Result<E, SagaResponse> result = futureResult.future().join();
        return result.fold(
                e -> toErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e),
                r -> r.result().fold(
                        e -> toSagaErrorResponse(HttpStatus.BAD_REQUEST, r.sagaId.id, e),
                        s -> ResponseEntity.ok(s)));
    }

    protected ResponseEntity toSagaErrorResponse(HttpStatus status, UUID sagaId, NonEmptyList<SagaError> reasons) {
        return ResponseEntity.status(status)
                .body(new SagaErrorDto(sagaId, reasons.toList()));
    }

    protected <E> ResponseEntity toErrorResponse(HttpStatus status, NonEmptyList<E> reasons) {
        return ResponseEntity.status(status)
                .body(reasons.toList());
    }
}
