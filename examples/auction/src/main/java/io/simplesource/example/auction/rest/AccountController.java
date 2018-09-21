package io.simplesource.example.auction.rest;

import io.simplesource.api.CommandAPI;
import io.simplesource.data.Sequence;
import io.simplesource.data.FutureResult;
import io.simplesource.example.auction.account.service.AccountWriteService;
import io.simplesource.example.auction.account.domain.Account;
import io.simplesource.example.auction.account.domain.Reservation;
import io.simplesource.example.auction.account.domain.ReservationId;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.rest.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@RestController
@RequestMapping(value = "/auction-example/accounts")
public final class AccountController extends BaseController {

    @Autowired
    private AccountWriteService accountWriteService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity createAccount(@Valid @RequestBody CreateAccountDto createAccountDto) {
        Account account = toDomainAccount(createAccountDto.getAccountDto());
        FutureResult<CommandAPI.CommandError, Sequence> result = accountWriteService.createAccount(toAccountKey(createAccountDto.getAccountId()), account);
        return toResponseEntity(result);
    }

    @RequestMapping(value = "/{accountId}", method = RequestMethod.PUT)
    public ResponseEntity updateAccount(@NotNull @PathVariable UUID accountId, @Valid @RequestBody UpdateAccountDto updateAccountDto) {
        FutureResult<CommandAPI.CommandError, UUID> result = accountWriteService.updateAccount(toAccountKey(accountId), updateAccountDto.getUserName());

        return toResponseEntity(result);
    }

    @RequestMapping(value = "/{accountId}/funds", method = RequestMethod.POST)
    public ResponseEntity addFunds(@NotNull @PathVariable UUID accountId, @Valid @RequestBody AddAccountFundsDto addAccountFundsDto) {
        FutureResult<CommandAPI.CommandError, Sequence> result = accountWriteService.addFunds(toAccountKey(accountId),
                Money.valueOf(addAccountFundsDto.getFunds()));

        return toResponseEntity(result);
    }

    @RequestMapping(value = "/{accountId}/funds/reservations", method = RequestMethod.POST)
    public ResponseEntity reserveFunds(@NotNull @PathVariable UUID accountId, @Valid @RequestBody ReserveFundsDto reserveFundsDto) {
        ReservationId reservationId = new ReservationId(reserveFundsDto.getReservationId());
        FutureResult<CommandAPI.CommandError, Sequence> result = accountWriteService.reserveFunds(
                toAccountKey(accountId), reservationId, new Reservation(reservationId, reserveFundsDto.getDescription(),
                        Money.valueOf(reserveFundsDto.getAmount()), Reservation.Status.DRAFT));

        return toResponseEntity(result);
    }

    @RequestMapping(value = "/{accountId}/funds/reservations/{reservationId}", method = RequestMethod.DELETE)
    public ResponseEntity cancelReservation(@NotNull @PathVariable UUID accountId, @NotNull @PathVariable UUID reservationId) {
        FutureResult<CommandAPI.CommandError, Sequence> result = accountWriteService.cancelReservation(toAccountKey(accountId),
                new ReservationId(reservationId));

        return toResponseEntity(result);
    }

    @RequestMapping(value = "/{accountId}/funds/reservations/{reservationId}", method = RequestMethod.POST)
    public ResponseEntity confirmReservation(@NotNull @PathVariable UUID accountId, @NotNull @PathVariable UUID reservationId,
                                             @NotNull @Valid @RequestBody ConfirmReservationDto confirmReservationDto) {
        FutureResult<CommandAPI.CommandError, Sequence> result = accountWriteService.confirmReservation(toAccountKey(accountId),
                new ReservationId(reservationId), Money.valueOf(confirmReservationDto.getAmount()));

        return toResponseEntity(result);
    }
}
