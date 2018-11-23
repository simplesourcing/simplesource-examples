package io.simplesource.example.user.json;

import io.simplesource.api.CommandAPI;
import io.simplesource.api.CommandAPISet;
import io.simplesource.api.CommandError;
import io.simplesource.data.Result;
import io.simplesource.data.Sequence;
import io.simplesource.example.user.UserAggregate;
import io.simplesource.example.user.domain.User;
import io.simplesource.example.user.domain.UserCommand;
import io.simplesource.example.user.domain.UserEvent;
import io.simplesource.example.user.domain.UserKey;
import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.api.CommandSerdes;
import io.simplesource.kafka.dsl.EventSourcedApp;
import io.simplesource.kafka.dsl.EventSourcedClient;
import io.simplesource.kafka.serialization.json.JsonCommandSerdes;
import io.simplesource.kafka.util.PrefixResourceNamingStrategy;
import io.simplesource.kafka.serialization.json.JsonAggregateSerdes;
import io.simplesource.kafka.spec.AggregateSetSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static io.simplesource.example.user.UserDomainCommandExample.submitCommands;
import static io.simplesource.kafka.serialization.json.JsonGenericMapper.jsonDomainMapper;
import static io.simplesource.kafka.serialization.json.JsonOptionalGenericMapper.jsonOptionalDomainMapper;

/**
 * This example demonstrates serializing with Json
 */
public final class UserJsonRunner {
    private static final Logger logger = LoggerFactory.getLogger(UserJsonRunner.class);

    private static PrefixResourceNamingStrategy namingStrategy = new PrefixResourceNamingStrategy("user_json_");
    private static final String aggregateName = "example-user";
    private static final String bootstrapServers = "localhost:9092";

    private final static AggregateSerdes<UserKey, UserCommand, UserEvent, Optional<User>> aggregateSerdes =
            new JsonAggregateSerdes<>(
                    jsonDomainMapper(),
                    jsonDomainMapper(),
                    jsonDomainMapper(),
                    jsonOptionalDomainMapper());

    public static void main(final String[] args) {

        final CommandAPI<UserKey, UserCommand> api = startAll().getCommandAPI(aggregateName);

        // publish some commands
        logger.info("Started publishing commands");
        final Result<CommandError, Sequence> result =
                submitCommands(api).unsafePerform(e -> CommandError.of(CommandError.Reason.InternalError, e));
        logger.info("Result of commands {}", result);
        logger.info("All commands published");
    }

    private static CommandAPISet startAll() {
        return new EventSourcedApp()
            .withKafkaConfig(builder ->
                    builder
                        .withKafkaApplicationId("userMappedJsonApp1")
                        .withKafkaBootstrap(bootstrapServers)
                        .build())
            .<UserKey, UserCommand, UserEvent, Optional<User>>addAggregate(builder -> builder
                    .withName(aggregateName)
                    .withSerdes(aggregateSerdes)
                    .withResourceNamingStrategy(namingStrategy)
                    .withInitialValue((k) -> Optional.empty())
                    .withAggregator(UserEvent.getAggregator())
                    .withCommandHandler(UserCommand.getCommandHandler())
            )
            .start()
            .getCommandAPISet(aggregateName);
    }
}
