package io.simplesource.example.auction.client.controller;

import io.simplesource.data.FutureResult;
import io.simplesource.data.Sequence;
import io.simplesource.example.auction.domain.*;
import io.simplesource.example.auction.client.service.AccountWriteService;
import io.simplesource.example.auction.client.dto.*;
import io.simplesource.example.auction.core.Money;
import io.simplesource.saga.model.messages.SagaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping(value = "/auction-example/accounts")
public final class AccountController extends BaseController {

    @Autowired
    private AccountWriteService accountWriteService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity createAccount(@Valid @RequestBody CreateAccountDto createAccountDto) {
        Account account = toDomainAccount(createAccountDto.getAccountDto());
        FutureResult<AccountError, SagaResponse> result = accountWriteService.createAccount(toAccountKey(createAccountDto.getAccountId()), account);
        return toSagaResponseEntity(result);
    }

    @RequestMapping(value = "/{accountId}", method = RequestMethod.PUT)
    public ResponseEntity updateAccount(@NotNull @PathVariable UUID accountId, @Valid @RequestBody UpdateAccountDto updateAccountDto) {
        FutureResult<AccountError, SagaResponse> result = accountWriteService.updateAccount(toAccountKey(accountId), updateAccountDto.getUserName());

        return toSagaResponseEntity(result);
    }

    @RequestMapping(value = "/{accountId}/funds", method = RequestMethod.POST)
    public ResponseEntity addFunds(@NotNull @PathVariable UUID accountId, @Valid @RequestBody AddAccountFundsDto addAccountFundsDto) {
        FutureResult<AccountError, Sequence> result = accountWriteService.addFunds(toAccountKey(accountId),
                Money.valueOf(addAccountFundsDto.getFunds()));

        return toResponseEntity(result);
    }

    private Account toDomainAccount(AccountDto accountDto) {
        BigDecimal funds = accountDto.getFunds();
        return Account.builder().username(accountDto.getUserName())
                .funds(Money.valueOf(funds))
                .build();
    }
}
