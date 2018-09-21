package io.simplesource.example.auction.rest;

import io.simplesource.api.CommandAPI;
import io.simplesource.data.FutureResult;
import io.simplesource.data.Result;
import io.simplesource.example.auction.account.domain.Account;
import io.simplesource.example.auction.account.domain.AccountKey;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.rest.dto.AccountDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

class BaseController {

    AccountKey toAccountKey(UUID accountId) {
        return new AccountKey(accountId);
    }

    protected <T> ResponseEntity toResponseEntity(FutureResult<CommandAPI.CommandError, T> futureResult) {
        Result<CommandAPI.CommandError, T> result = futureResult.future().join();
        if ( result.isSuccess() ) {
            return ResponseEntity.ok(result.getOrElse(null));
        }
        ResponseEntity.BodyBuilder responseBodyBuilder = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
        return result.failureReasons().map(responseBodyBuilder::body).orElse(null);
    }

    protected Account toDomainAccount(AccountDto accountDto) {
        BigDecimal funds = accountDto.getFunds();
        return Account.builder().username(accountDto.getUserName())
                .funds(Money.valueOf(funds))
                .build();
    }
}
