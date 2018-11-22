package io.simplesource.example.auction.account.service;

import io.simplesource.api.CommandAPI;
import io.simplesource.api.CommandError;
import io.simplesource.data.FutureResult;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Result;
import io.simplesource.data.Sequence;
import io.simplesource.example.auction.account.domain.*;
import io.simplesource.example.auction.account.domain.AccountError.Reason;
import io.simplesource.example.auction.account.query.repository.AccountRepository;
import io.simplesource.example.auction.account.query.repository.AccountTransactionRepository;
import io.simplesource.example.auction.account.query.views.AccountTransactionView;
import io.simplesource.example.auction.account.query.views.AccountTransactionViewKey;
import io.simplesource.example.auction.account.query.views.AccountView;
import io.simplesource.example.auction.core.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class AccountWriteServiceImpl implements AccountWriteService {
    private Logger logger = LoggerFactory.getLogger(AccountWriteServiceImpl.class);
    private static final Function<CommandError, AccountError> COMMAND_ERROR_TO_ACCOUNT_ERROR_FUNCTION =
            ce -> AccountError.of(Reason.CommandError, ce.getReason() + ":" + ce.getMessage());

    private final AccountRepository accountRepository;
    private final CommandAPI<AccountKey, AccountCommand> accountCommandAPI;
    private final AccountTransactionRepository accountTransactionRepository;

    public AccountWriteServiceImpl(CommandAPI<AccountKey, AccountCommand> accountCommandAPI, AccountRepository accountRepository, AccountTransactionRepository accountTransactionRepository) {
        this.accountCommandAPI = accountCommandAPI;
        this.accountRepository = accountRepository;
        this.accountTransactionRepository = accountTransactionRepository;
    }

    @Override
    public FutureResult<AccountError, Sequence> createAccount(AccountKey accountKey, Account account) {
        requireNonNull(account);
        requireNonNull(accountKey);

        Optional<AccountView> existingAccount = accountRepository
                .findByAccountId(accountKey.id().toString());

        List<AccountError> validationErrorReasons =
                Stream.of(
                        validUsername(account.username()),
                        validateFunds(account.funds(), "Initial fund can not be negative"),
                        existingAccount.map(acc -> AccountError.of(Reason.AccountIdAlreadyExist,
                                String.format("Account ID %s already exist", acc.getId()))),
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
        return this.commandAndQueryAccount(accountKey, Sequence.first(), command, Duration.ofMinutes(1));
    }

    @Override
    public FutureResult<AccountError, Sequence> updateAccount(AccountKey accountKey, String username) {
        Objects.requireNonNull(accountKey);

        List<AccountError> validationErrorReasons = Stream.of(validUsername(username),
                usernameNotTakenBefore(accountKey, username)).filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());

        if (!validationErrorReasons.isEmpty()) {
            return FutureResult.fail(NonEmptyList.fromList(validationErrorReasons));
        }

        return commandAndQueryExistingAccount(accountKey, new AccountCommand.UpdateAccount(username), Duration.ofMinutes(1));
    }

    @Override
    public FutureResult<AccountError, Sequence> addFunds(@NotNull AccountKey accountKey, @NotNull Money funds) {
        Optional<AccountError> invalidAmount = validateFunds(funds, "Cannot add negative fund amount");

        return invalidAmount.<FutureResult<AccountError, Sequence>>map(FutureResult::fail)
                .orElse(commandAndQueryExistingAccount(accountKey, new AccountCommand.AddFunds(funds), Duration.ofMinutes(1)));
    }

    @Override
    public FutureResult<AccountError, Sequence> reserveFunds(@NotNull AccountKey accountKey, @NotNull ReservationId reservationId,
                                                             @NotNull Reservation reservation) {
        Optional<AccountTransactionView> existingReservation = loadAccountReservation(accountKey, reservationId);

        Optional<AccountView> existingAccount = accountRepository
                .findByAccountId(accountKey.id().toString());

        List<AccountError> validationErrorReasons = Stream.of(
                existingReservation.map(res -> AccountError.of(Reason.ReservationIdAlreadyExist,
                        String.format("Reservation with ID %s already exist", reservationId))),
                existingAccount.flatMap(acc -> validateFunds(reservation.amount(), "Cannot reserve a negative amount")))
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.toList());

        if (!validationErrorReasons.isEmpty()) {
            return FutureResult.fail(NonEmptyList.fromList(validationErrorReasons));
        }

        return commandAndQueryExistingAccount(accountKey, new AccountCommand.ReserveFunds(reservationId, reservation.amount(),
                reservation.description()), Duration.ofMinutes(1));
    }

    @Override
    public FutureResult<AccountError, Sequence> cancelReservation(AccountKey accountKey, ReservationId reservationId) {
        AccountCommand.CancelReservation command = new AccountCommand.CancelReservation(reservationId);

        Optional<AccountError> mayBeInvalid = validateReservationForAccount(accountKey, reservationId);
        return mayBeInvalid
                .<FutureResult<AccountError, Sequence>>map(FutureResult::fail)
                .orElse(commandAndQueryExistingAccount(accountKey, command, Duration.ofMinutes(1)));
    }

    @Override
    public FutureResult<AccountError, Sequence> confirmReservation(AccountKey accountKey, ReservationId reservationId, Money amount) {
        AccountCommand.ConfirmReservation command = new AccountCommand.ConfirmReservation(reservationId, amount);

        List<AccountError> validationErrorReasons = Stream.of(
                validateFunds(amount, "Confirmed transaction cannot have a negative amount")
        )
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.toList());

        if (!validationErrorReasons.isEmpty()) {
            return FutureResult.fail(NonEmptyList.fromList(validationErrorReasons));
        }

        Optional<AccountError> mayBeInvalid = validateReservationForAccount(accountKey, reservationId);
        return mayBeInvalid
                .<FutureResult<AccountError, Sequence>>map(FutureResult::fail)
                .orElse(commandAndQueryExistingAccount(accountKey, command, Duration.ofMinutes(1)));
    }

    private <C extends AccountCommand> FutureResult<AccountError, Sequence> commandAndQueryExistingAccount(AccountKey accountKey,
                                                                                                           C command,
                                                                                                           Duration duration) {
        Optional<AccountView> maybeAccount = accountRepository.findByAccountId(accountKey.asString());

        return maybeAccount
                .map(a -> commandAndQueryAccount(accountKey, Sequence.position(a.getLastEventSequence()), command, duration))
                .orElse(FutureResult.fail(AccountError.of(Reason.AccountDoesNotExist)));
    }

    private <C extends AccountCommand> FutureResult<AccountError, Sequence> commandAndQueryAccount(AccountKey accountKey,
                                                                                                   Sequence sequence, C command,
                                                                                                   Duration duration) {

        FutureResult<CommandError, Sequence> commandResult = accountCommandAPI.publishCommand(new CommandAPI.Request<>(accountKey,
                sequence, UUID.randomUUID(), command), duration);

        Function<NonEmptyList<CommandError>, Result<AccountError, Sequence>> failureMapFunc =
                ers -> Result.failure(ers.map(COMMAND_ERROR_TO_ACCOUNT_ERROR_FUNCTION));

        Future<Result<AccountError, Sequence>> future = commandResult.fold(failureMapFunc, Result::success);

        return FutureResult.ofFutureResult(future, exp -> AccountError.of(Reason.UnknownError, exp.getMessage()));
    }

    private Optional<AccountError> validateReservationForAccount(AccountKey accountKey, ReservationId reservationId) {
        Optional<AccountView> mayBeAccount = accountRepository.findByAccountId(accountKey.id().toString());
        if (!mayBeAccount.isPresent()) {
            return Optional.of(AccountError.of(Reason.AccountDoesNotExist));
        }

        Optional<AccountTransactionView> accountReservation = loadAccountReservation(accountKey, reservationId);
        if (!accountReservation.isPresent())
            return Optional.of(AccountError.of(Reason.ReservationDoesNotExist,
                    String.format("Reservation with ID %s does not exist for account with ID %s", reservationId, accountKey)));

        if (!accountReservation.filter(v -> v.getStatus() == Reservation.Status.DRAFT).isPresent()) {
            return Optional.of(AccountError.of(Reason.InvalidData, "Account reservation is not in draft state"));
        }

        return Optional.empty();
    }

    private Optional<AccountError> validUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return Optional.of(AccountError.of(Reason.InvalidData, "Username can not be empty"));
        }

        return Optional.empty();
    }

    private Optional<AccountError> usernameNotTakenBefore(AccountKey accountKey, String username) {
        List<AccountView> usersWithName = accountRepository.findOtherAccountsWithUsername(accountKey.asString(), username);
        return usersWithName.stream().findAny()
                .map(v -> AccountError.of(Reason.UserNameIsNotAvailable, String.format("User name %s already exists", username)));
    }

    private Optional<AccountError> validateFunds(Money funds, String msg) {
        if (funds == null || funds.isNegativeAmount())
            return Optional.of(AccountError.of(Reason.InvalidData, msg));

        return Optional.empty();
    }

    private Optional<AccountTransactionView> loadAccountReservation(AccountKey accountKey, ReservationId reservationId) {
        return accountTransactionRepository
                .findByTransactionKey(new AccountTransactionViewKey(accountKey, reservationId));
    }
}
