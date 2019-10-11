package io.simplesource.example.demo.repository.write.simplesource;

import io.simplesource.api.CommandAPI;
import io.simplesource.api.CommandError;
import io.simplesource.api.CommandId;
import io.simplesource.data.FutureResult;
import io.simplesource.data.Result;
import io.simplesource.data.Sequence;
import io.simplesource.example.demo.domain.Account;
import io.simplesource.example.demo.repository.write.AccountWriteRepository;
import io.simplesource.example.demo.repository.write.CreateAccountError;
import io.simplesource.kafka.model.CommandRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.Optional;

/**
 * Use Simplesourcing as the write store
 */
public class SimplesourceAccountRepository implements AccountWriteRepository {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);

    private CommandAPI<String, AccountCommand> commandApi;

    public SimplesourceAccountRepository(@Autowired CommandAPI<String, AccountCommand> commandApi){
        this.commandApi = commandApi;
    }

    @Override
    public Optional<CreateAccountError> create(Account account) {
       FutureResult<CommandError, Sequence> result = commandApi.publishAndQueryCommand(new CommandAPI.Request<>(CommandId.random(), account.name, Sequence.first(), new AccountCommand.CreateAccount(account.name, account.balance)), DEFAULT_TIMEOUT);

       //TODO handle future resolution and error handling properly, below is a quick hacky just do it implementation
        final Result<CommandError, Sequence> resolved = result.unsafePerform(e -> CommandError.of(CommandError.Reason.CommandHandlerFailed, e.getMessage()));


        if(resolved.failureReasons().isPresent()){
            if(resolved.failureReasons().get().head().getReason() == CommandError.Reason.InvalidReadSequence) {
                return Optional.of(CreateAccountError.ACCOUNT_ALREADY_EXISTS);
            }

            Optional<CreateAccountError> error = CreateAccountError.fromString(resolved.failureReasons().get().head().getMessage());

            if(error.isPresent()) {
                return error;
            } else {
                throw new RuntimeException(resolved.failureReasons().get().head().getMessage());
            }
        }

        return Optional.empty();
    }
}
