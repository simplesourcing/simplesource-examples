package io.simplesource.example.demo.repository.write.simplesource;

import io.simplesource.api.CommandError;
import io.simplesource.api.CommandHandler;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Result;
import io.simplesource.example.demo.repository.write.CreateAccountError;

import java.util.Optional;

public final class AccountCommandHandler implements CommandHandler<String, AccountCommand, AccountEvent, Optional<Account>> {
    private static AccountCommandHandler instance = null;

    public static AccountCommandHandler getInstance() {
        if(instance == null) {
            instance = new AccountCommandHandler();
        }

        return  instance;
    }

    @Override
    public Result<CommandError, NonEmptyList<AccountEvent>> interpretCommand(String key, Optional<Account> currentAggregate, AccountCommand command) {
        return command.match(
                createAccount -> createAccountHandler(key, currentAggregate, createAccount),
                deposit -> depositHandler(key, currentAggregate, deposit),
                withdraw -> withdrawHandler(key, currentAggregate, withdraw)
        );
    }

    public static Result<CommandError, NonEmptyList<AccountEvent>> createAccountHandler(String key, Optional<Account> currentAggregate, AccountCommand.CreateAccount command) {
        return currentAggregate
                .<Result<CommandError, NonEmptyList<AccountEvent>>>map(account -> Result.failure(CommandError.of(CommandError.Reason.CommandHandlerFailed, CreateAccountError.ACCOUNT_ALREADY_EXISTS.message())))
                .orElseGet(() -> {
                    if (command.name == null || command.name.trim().isEmpty()) {
                        return Result.failure(CommandError.of(CommandError.Reason.CommandHandlerFailed, CreateAccountError.ACCOUNT_NAME_NOT_SET.message()));
                    }

                    if (command.openingBalance < 0) {
                        return Result.failure(CommandError.of(CommandError.Reason.CommandHandlerFailed, CreateAccountError.OPENING_BALANCE_LESS_THAN_ZERO.message()));
                    }

                    return Result.success(NonEmptyList.of(new AccountEvent.AccountCreated(command.name, command.openingBalance)));
                });
    }

    public static Result<CommandError, NonEmptyList<AccountEvent>> depositHandler(String key, Optional<Account> currentAggregate, AccountCommand.Deposit command) {
        return currentAggregate
                .<Result<CommandError, NonEmptyList<AccountEvent>>>map(account -> {
                    if (command.amount <= 0) {
                        return Result.failure(CommandError.of(CommandError.Reason.CommandHandlerFailed, "Amount must be greater than 0"));
                    } else {
                        return Result.success(NonEmptyList.of(new AccountEvent.Deposited(command.accountName, account.balance + command.amount)));
                    }
                })
                .orElse(Result.failure(CommandError.of(CommandError.Reason.CommandHandlerFailed, "Account does not exist")));
    }

    public static Result<CommandError, NonEmptyList<AccountEvent>> withdrawHandler(String key, Optional<Account> currentAggregate, AccountCommand.Withdraw command) {
        return currentAggregate
                .<Result<CommandError, NonEmptyList<AccountEvent>>>map(account -> {
                    if (command.amount <= 0) {
                        return Result.failure(CommandError.of(CommandError.Reason.CommandHandlerFailed, "Amount needs to be greater than 0"));
                    } else if (account.balance - command.amount < 0) {
                        return Result.failure(CommandError.of(CommandError.Reason.CommandHandlerFailed, "Insufficient funds"));
                    } else {
                        return Result.success(NonEmptyList.of(new AccountEvent.Deposited(command.accountName, account.balance + command.amount)));
                    }
                })
                .orElse(Result.failure(CommandError.of(CommandError.Reason.CommandHandlerFailed, "Account does not exist")));
    }
}