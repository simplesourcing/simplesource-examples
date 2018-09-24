package io.simplesource.example.auction.account.service;

import io.simplesource.api.CommandAPI;
import io.simplesource.data.Sequence;
import io.simplesource.data.FutureResult;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Reason;
import io.simplesource.example.auction.account.domain.*;
import io.simplesource.example.auction.account.query.views.AccountView;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.account.query.views.AccountTransactionViewKey;
import io.simplesource.example.auction.account.query.views.AccountTransactionView;
import io.simplesource.example.auction.account.query.repository.AccountRepository;
import io.simplesource.example.auction.account.query.repository.AccountTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class AccountWriteServiceImpl implements AccountWriteService {
    private Logger logger = LoggerFactory.getLogger(AccountWriteServiceImpl.class);

    private final AccountRepository accountRepository;
    private final CommandAPI<AccountKey, AccountCommand> accountCommandAPI;
    private final AccountTransactionRepository accountTransactionRepository;

    public AccountWriteServiceImpl(CommandAPI<AccountKey, AccountCommand> accountCommandAPI, AccountRepository accountRepository, AccountTransactionRepository accountTransactionRepository) {
        this.accountCommandAPI = accountCommandAPI;
        this.accountRepository = accountRepository;
        this.accountTransactionRepository = accountTransactionRepository;
    }

    @Override
    public FutureResult<CommandAPI.CommandError, Sequence> createAccount(AccountKey accountKey, Account account) {
        requireNonNull(account);
        requireNonNull(accountKey);

        Optional<AccountView> existingAccount = accountRepository
                .findByAccountId(accountKey.id().toString());

        List<Reason<CommandAPI.CommandError>> validationErrorReasons =
                Stream.of(
                        validUsername(account.username()),
                        validateFunds(account.funds(), "Initial fund can not be negative"),
                        existingAccount.map(acc -> Reason.of(CommandAPI.CommandError.InternalError, "Account with same ID already exist")),
                        usernameNotTakenBefore(accountKey, account.username())
                )
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        if (!validationErrorReasons.isEmpty()) {
            return FutureResult.fail(NonEmptyList.fromList(validationErrorReasons));
        }

        logger.info("Creating account with username {} and initial fund is {}", account.username(), account.funds());
        AccountCommand.CreateAccount command = new AccountCommand.CreateAccount(account.username(), account.funds());
        return accountCommandAPI.publishAndQueryCommand(new CommandAPI.Request<>(accountKey, Sequence.first(), UUID.randomUUID(), command),
                Duration.ofMinutes(1)).map(NonEmptyList::last);

    }

    //TODO Change the service to return AccountError and Sequence
    @Override
    public FutureResult<CommandAPI.CommandError, UUID> updateAccount(AccountKey accountKey, String username) {
        Objects.requireNonNull(accountKey);

        List<Reason<CommandAPI.CommandError>> validationErrorReasons = Stream.of(validUsername(username),
                usernameNotTakenBefore(accountKey, username)).filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());

        if (!validationErrorReasons.isEmpty()) {
            return FutureResult.fail(NonEmptyList.fromList(validationErrorReasons));
        }

        return sendCommandForAccount(accountKey, new AccountCommand.UpdateAccount(username));
    }

    @Override
    public FutureResult<CommandAPI.CommandError, Sequence> addFunds(@NotNull AccountKey accountKey, @NotNull Money funds) {
        Optional<Reason<CommandAPI.CommandError>> invalidAmount = validateFunds(funds, "Cannot add a negative amount");

        if (invalidAmount.isPresent())
            return FutureResult.fail(invalidAmount.get());

        FutureResult<CommandAPI.CommandError, UUID> commandResult = sendCommandForAccount(accountKey,
                new AccountCommand.AddFunds(funds));

        return commandResult.flatMap(v -> accountCommandAPI.queryCommandResult(v, Duration.ofMinutes(1)).map(NonEmptyList::last));
    }

    @Override
    public FutureResult<CommandAPI.CommandError, Sequence> reserveFunds(@NotNull AccountKey accountKey, @NotNull ReservationId reservationId,
                                                                        @NotNull Reservation reservation) {
        Optional<AccountTransactionView> existingReservation = accountTransactionRepository
                .findByTransactionKey(new AccountTransactionViewKey(accountKey, reservationId));

        Optional<AccountView> existingAccount = accountRepository
                .findByAccountId(accountKey.id().toString());

        List<Reason<CommandAPI.CommandError>> validationErrorReasons = Stream.of(
                existingReservation.map(res -> Reason.of(CommandAPI.CommandError.InternalError, "Reservation with same ID already exist")),
                existingAccount.flatMap(acc -> validateReserveFunds(reservation.amount())))
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.toList());

        if (!validationErrorReasons.isEmpty()) {
            return FutureResult.fail(NonEmptyList.fromList(validationErrorReasons));
        }

        FutureResult<CommandAPI.CommandError, UUID> commandResult = sendCommandForAccount(accountKey, new AccountCommand.ReserveFunds(reservationId, reservation.amount(), reservation.description()));
        return commandResult.flatMap(v -> accountCommandAPI.queryCommandResult(v, Duration.ofMinutes(1)).map(NonEmptyList::last));
    }

    @Override
    public FutureResult<CommandAPI.CommandError, Sequence> cancelReservation(AccountKey accountKey, ReservationId reservationId) {
        if (!reservationExists(accountKey, reservationId))
            return FutureResult.fail(Reason.of(CommandAPI.CommandError.InternalError, "No reservation with the passed ID"));

        FutureResult<CommandAPI.CommandError, UUID> commandResult = sendCommandForAccount(accountKey, new AccountCommand.CancelReservation(reservationId));
        return commandResult.flatMap(v -> accountCommandAPI.queryCommandResult(v, Duration.ofMinutes(1)).map(NonEmptyList::last));
    }

    @Override
    public FutureResult<CommandAPI.CommandError, Sequence> confirmReservation(AccountKey accountKey, ReservationId reservationId, Money amount) {
        if (!reservationExists(accountKey, reservationId))
            return FutureResult.fail(Reason.of(CommandAPI.CommandError.InternalError, "No reservation with the passed ID"));

        Optional<AccountView> existingAccount = accountRepository
                .findByAccountId(accountKey.id().toString());

        Optional<AccountTransactionView> existingReservation = accountTransactionRepository
                .findByTransactionKey(new AccountTransactionViewKey(accountKey, reservationId));

        Optional<Reason<CommandAPI.CommandError>> reservationMissing = (existingReservation.isPresent() ? Optional.empty() : Optional.of(
                Reason.of(CommandAPI.CommandError.InternalError, "Reservation not present")));

        List<Reason<CommandAPI.CommandError>> validationErrorReasons = Stream.of(
                reservationMissing,
                existingAccount.flatMap(acc -> validateReserveFunds(amount)))
                .filter(inv -> inv.isPresent())
                .map(Optional::get).collect(Collectors.toList());

        if (!validationErrorReasons.isEmpty()) {
            return FutureResult.fail(NonEmptyList.fromList(validationErrorReasons));
        }

        FutureResult<CommandAPI.CommandError, UUID> commandResult = sendCommandForAccount(accountKey, new AccountCommand.ConfirmReservation(reservationId, amount));
        return commandResult.flatMap(v -> accountCommandAPI.queryCommandResult(v, Duration.ofMinutes(1)).map(NonEmptyList::last));
    }

    private boolean reservationExists(AccountKey accountKey, ReservationId reservationId) {
        Optional<AccountTransactionView> existingReservation = accountTransactionRepository
                .findByTransactionKey(new AccountTransactionViewKey(accountKey, reservationId));

        return existingReservation.isPresent();
    }

    private Optional<Reason<CommandAPI.CommandError>> validUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return Optional.of(Reason.of(CommandAPI.CommandError.CommandPublishError, "Username can not be empty"));
        }

        return Optional.empty();
    }

    private <C extends AccountCommand> FutureResult<CommandAPI.CommandError, UUID> sendCommandForAccount(AccountKey accountKey, C command) {
        Optional<AccountView> maybeAccount = accountRepository.findByAccountId(accountKey.asString());

        return maybeAccount
                .map(a -> accountCommandAPI.publishCommand(new CommandAPI.Request<>(accountKey,
                        Sequence.position(a.getLastEventSequence()), UUID.randomUUID(), command)))
                .orElse(FutureResult.fail(Reason.of(CommandAPI.CommandError.CommandPublishError, "Account does not exist")));
    }

    private Optional<Reason<CommandAPI.CommandError>> usernameNotTakenBefore(AccountKey accountKey, String username) {
        List<AccountView> usersWithName = accountRepository.findOtherAccountsWithUsername(accountKey.asString(), username);
        if (!usersWithName.isEmpty()) {
            return Optional.of(Reason.of(CommandAPI.CommandError.CommandPublishError, "User name already exists"));
        }

        return Optional.empty();
    }

    private Optional<Reason<CommandAPI.CommandError>> validateFunds(Money funds, String msg) {
        if (funds == null || funds.isNegativeAmount())
            return Optional.of(Reason.of(CommandAPI.CommandError.CommandPublishError, msg));

        return Optional.empty();
    }

    private Optional<Reason<CommandAPI.CommandError>> validateReserveFunds(Money funds) {
        if (funds == null || funds.isNegativeAmount())
            return Optional.of(Reason.of(CommandAPI.CommandError.CommandPublishError, "Cannot reserve a negative amount"));

        return Optional.empty();
    }
}
