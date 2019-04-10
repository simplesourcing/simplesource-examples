package io.simplesource.example.auction.client.controller;


import io.simplesource.example.auction.client.repository.AccountRepository;
import io.simplesource.example.auction.client.views.AccountTransactionView;
import io.simplesource.example.auction.client.views.AccountView;
import io.simplesource.example.auction.client.service.AccountReadService;
import io.simplesource.example.auction.client.service.AccountWriteService;
import io.simplesource.example.auction.client.dto.AccountDetailsDto;
import io.simplesource.example.auction.client.dtomappers.AccountEntityToDtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//Workaround to expose all projection read endpoints using the same base URI which is the same as the one used by spring data REST
@RestController
@RequestMapping(value = "/auction-example/projections/accounts")
public final class AccountProjectionController extends BaseController {
    @Autowired
    private AccountWriteService accountWriteService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountEntityToDtoMapper accountEntityToDtoMapper;
    @Autowired
    private AccountReadService accountReadService;

    @RequestMapping(value = "/{accountId}/transactions", method = RequestMethod.GET)
    public ResponseEntity accountTransactions(@NotNull @PathVariable UUID accountId) {
        List<AccountTransactionView> transactionHistory = accountReadService.getTransactionHistory(toAccountKey(accountId));

        return ResponseEntity.ok(transactionHistory);
    }

    @RequestMapping(value = "/{accountId}", method = RequestMethod.GET)
    public ResponseEntity accountDetails(@NotNull @PathVariable UUID accountId) {
        Optional<AccountView> accountDetails = accountRepository.findByAccountId(accountId.toString());

        return accountDetails.map(this::toAccountDto).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    private AccountDetailsDto toAccountDto(AccountView entity) {
        return accountEntityToDtoMapper.toDto(entity);
    }
}
