package io.simplesource.example.user;

import io.simplesource.api.CommandAPI;
import io.simplesource.api.CommandError;
import io.simplesource.data.FutureResult;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Sequence;
import io.simplesource.example.user.domain.UserCommand;
import io.simplesource.example.user.domain.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.UUID;

public final class UserDomainCommandExample {
    private static final Logger logger = LoggerFactory.getLogger(UserDomainCommandExample.class);

    public static FutureResult<CommandError, NonEmptyList<Sequence>> submitCommands(
            final CommandAPI<UserKey, UserCommand> commandAPI
    ) {
        final UserKey key = new UserKey("user" + System.currentTimeMillis());
        final String firstName = "Sarah";
        final String lastName = "Dubois";

        return commandAPI
            .publishAndQueryCommand(new CommandAPI.Request<>(
                key,
                Sequence.first(),
                UUID.randomUUID(),
                new UserCommand.InsertUser(firstName, lastName)),
                Duration.ofMinutes(2L)
            )
            .flatMap(sequences -> {
                logger.info("Received result {} new sequences", sequences);
                return commandAPI.publishAndQueryCommand(new CommandAPI.Request<>(
                    key,
                    sequences.last(),
                    UUID.randomUUID(),
                    new UserCommand.UpdateName("Sarah Jones", lastName)),
                    Duration.ofMinutes(2L)
                );
            });
    }
}
