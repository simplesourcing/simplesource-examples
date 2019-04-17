package io.simplesource.example.auction.client.service;

import io.simplesource.api.CommandAPI;
import io.simplesource.api.CommandError;
import io.simplesource.api.CommandId;
import io.simplesource.data.FutureResult;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Result;
import io.simplesource.data.Sequence;
import io.simplesource.example.auction.AppShared;
import io.simplesource.example.auction.account.wire.AccountSagaCommand;
import io.simplesource.example.auction.account.wire.CreateAccount;
import io.simplesource.example.auction.account.wire.UpdateAccount;
import io.simplesource.example.auction.allocation.wire.AllocationSagaCommand;
import io.simplesource.example.auction.allocation.wire.Claim;
import io.simplesource.example.auction.allocation.wire.Release;
import io.simplesource.example.auction.client.repository.AccountRepository;
import io.simplesource.example.auction.client.views.AccountView;
import io.simplesource.example.auction.command.AccountCommand;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.Account;
import io.simplesource.example.auction.domain.AccountError;
import io.simplesource.example.auction.domain.AccountError.Reason;
import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.saga.model.action.ActionCommand;
import io.simplesource.saga.model.action.ActionId;
import io.simplesource.saga.model.api.SagaAPI;
import io.simplesource.saga.model.messages.SagaRequest;
import io.simplesource.saga.model.messages.SagaResponse;
import io.simplesource.saga.model.saga.Saga;
import io.simplesource.saga.model.saga.SagaError;
import io.simplesource.saga.model.saga.SagaId;
import io.simplesource.saga.client.dsl.SagaDSL;
import org.apache.avro.generic.GenericRecord;
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
    private final SagaAPI<GenericRecord> sagaAPI;
    private final Duration sagaResponseTimeout = Duration.ofMillis(10000);

    public AccountWriteServiceImpl(
            CommandAPI<AccountKey, AccountCommand> accountCommandAPI,
            SagaAPI<GenericRecord> sagaAPI,
            AccountRepository accountRepository) {
        this.accountCommandAPI = accountCommandAPI;
        this.sagaAPI = sagaAPI;
        this.accountRepository = accountRepository;
    }

    /**
     * Ensures username uniqueness by submitting a saga to claim the username before actually creating the account.
     */
    @Override
    public FutureResult<AccountError, SagaResponse> createAccount(AccountKey accountKey, Account account) {
        requireNonNull(account);
        requireNonNull(accountKey);

        Optional<AccountView> existingAccount = accountRepository
                .findByAccountId(accountKey.id().toString());

        List<AccountError> validationErrorReasons =
                Stream.of(
                        validUsername(account.username()),
                        validateFunds(account.funds(), "Initial funds can not be negative"),
                        existingAccount.map(acc -> AccountError.of(Reason.AccountIdAlreadyExist,
                                String.format("Account ID %s already exist", acc.getId())))
                )
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        // we must use a saga to allocate the username before actually creating the account
        return NonEmptyList.fromList(validationErrorReasons)
                .map(FutureResult::<AccountError, SagaResponse>fail)
                .orElseGet(() -> {
                    logger.info("Creating account with username {} and initial fund is {}", account.username(), account.funds());
                    SagaDSL.SagaBuilder<GenericRecord> sagaBuilder = SagaDSL.SagaBuilder.create();
                    sagaBuilder.addAction(
                            ActionId.random(),
                            AppShared.USERNAME_ALLOCATION_AGGREGATE_NAME,
                            new AllocationSagaCommand(account.username(), new Claim())
                    ).andThen(sagaBuilder.addAction(
                            ActionId.random(),
                            AppShared.ACCOUNT_AGGREGATE_NAME,
                            new AccountSagaCommand(accountKey.asString(), new CreateAccount(
                                    account.username(), account.funds().getAmount()), 0L)
                    ));
                    Result<SagaError, Saga<GenericRecord>> built = sagaBuilder.build();
                    return built.fold(
                            e -> FutureResult.fail(AccountError.of(AccountError.Reason.UnknownError)),
                            s -> submitAndQuerySagaResponse(SagaRequest.of(SagaId.random(), s))
                    );
                });
    }

    /**
     * Ensures username uniqueness by submitting a saga to claim the new username before actually updating the account
     * and releasing the old username. Notice we don't release the existing username first because if the rest of the
     * saga fails it may not be possible to claim it back.
     */
    @Override
    public FutureResult<AccountError, SagaResponse> updateAccount(AccountKey accountKey, String username) {
        Objects.requireNonNull(accountKey);

        List<AccountError> validationErrorReasons = Stream.of(
                validUsername(username)
        )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());


        Optional<AccountView> existingAccount = accountRepository
                .findByAccountId(accountKey.id().toString());

        return NonEmptyList.fromList(validationErrorReasons)
                .map(FutureResult::<AccountError, SagaResponse>fail)
                .orElseGet(() ->
                        existingAccount.map(account -> {
                            String previousUsername = account.getUserName();
                            SagaDSL.SagaBuilder<GenericRecord> sagaBuilder = SagaDSL.SagaBuilder.create();
                            sagaBuilder.addAction(
                                    ActionId.random(),
                                    AppShared.USERNAME_ALLOCATION_AGGREGATE_NAME,
                                    new AllocationSagaCommand(username, new Claim())
                            ).andThen(sagaBuilder.addAction(
                                    ActionId.random(),
                                    AppShared.ACCOUNT_AGGREGATE_NAME,
                                    new AccountSagaCommand(accountKey.asString(), new UpdateAccount(
                                            username), account.getLastEventSequence())
                            )).andThen(sagaBuilder.addAction(
                                    ActionId.random(),
                                    AppShared.USERNAME_ALLOCATION_AGGREGATE_NAME,
                                    new AllocationSagaCommand(previousUsername, new Release())
                            ));
                            return sagaBuilder.build().fold(
                                    e -> FutureResult.<AccountError, SagaResponse>fail(AccountError.of(Reason.UnknownError)),
                                    s -> sagaAPI.submitSaga(SagaRequest.<GenericRecord>of(SagaId.random(), s))
                                            .flatMap(u -> sagaAPI.getSagaResponse((SagaId) u, sagaResponseTimeout))
                                            .errorMap(e -> AccountError.of(Reason.CommandError))
                            );
                        }).orElse(FutureResult.<AccountError, SagaResponse>fail(AccountError.of(Reason.AccountDoesNotExist))));
    }

    @Override
    public FutureResult<AccountError, Sequence> addFunds(@NotNull AccountKey accountKey, @NotNull Money funds) {
        Optional<AccountError> invalidAmount = validateFunds(funds, "Cannot add a negative amount");

        return invalidAmount.<FutureResult<AccountError, Sequence>>map(FutureResult::fail)
                .orElse(commandAndQueryExistingAccount(accountKey, new AccountCommand.AddFunds(funds), Duration.ofMinutes(1)));
    }

    private <C extends AccountCommand> FutureResult<AccountError, Sequence> commandAndQueryExistingAccount(AccountKey accountKey,
                                                                                                           C command,
                                                                                                           Duration duration) {
        Optional<AccountView> maybeAccount = accountRepository.findByAccountId(accountKey.asString());

        return maybeAccount
                .map(a -> commandAndQueryAccount(accountKey, Sequence.position(a.getLastEventSequence()), command, duration))
                .orElse(FutureResult.fail(AccountError.of(Reason.AccountDoesNotExist)));
    }

    public FutureResult<AccountError, SagaResponse> submitAndQuerySagaResponse(SagaRequest<GenericRecord> sagaRequest) {
        return sagaAPI.submitSaga(sagaRequest)
                .flatMap(u -> sagaAPI.getSagaResponse(u, sagaResponseTimeout))
                .errorMap(e -> AccountError.of(AccountError.Reason.CommandError));
    }

    private <C extends AccountCommand> FutureResult<AccountError, Sequence> commandAndQueryAccount(AccountKey accountKey,
                                                                                                   Sequence sequence, C command,
                                                                                                   Duration duration) {

        FutureResult<CommandError, Sequence> commandResult = accountCommandAPI.publishAndQueryCommand(new CommandAPI.Request<>(CommandId.random(),
                accountKey, sequence, command), duration);

        Function<NonEmptyList<CommandError>, Result<AccountError, Sequence>> failureMapFunc =
                ers -> Result.failure(ers.map(COMMAND_ERROR_TO_ACCOUNT_ERROR_FUNCTION));

        Future<Result<AccountError, Sequence>> future = commandResult.fold(failureMapFunc, Result::success);

        return FutureResult.ofFutureResult(future, exp -> AccountError.of(Reason.UnknownError, exp.getMessage()));
    }

    private Optional<AccountError> validUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return Optional.of(AccountError.of(Reason.InvalidData, "Username can not be empty"));
        }

        return Optional.empty();
    }

    private Optional<AccountError> validateFunds(Money funds, String msg) {
        if (funds == null || funds.isNegativeAmount())
            return Optional.of(AccountError.of(Reason.InvalidData, msg));

        return Optional.empty();
    }
}
