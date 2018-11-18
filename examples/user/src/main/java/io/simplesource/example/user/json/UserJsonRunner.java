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
import io.simplesource.kafka.dsl.EventSourcedApp;
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
 * This example demonstrates serializing with Avro but providing our own custom domain classes
 * for project, event command and key.
 *
 * @see UserAggregate
 */
public final class UserJsonRunner {
    private static final Logger logger = LoggerFactory.getLogger(UserJsonRunner.class);

    public static void main(final String[] args) {
        final AggregateSerdes<UserKey, UserCommand, UserEvent, Optional<User>> aggregateSerdes =
            new JsonAggregateSerdes<>(
                jsonDomainMapper(),
                jsonDomainMapper(),
                jsonDomainMapper(),
                jsonOptionalDomainMapper());

        final String aggregateName = "example-user";
        CommandAPISet commandApiSet = new EventSourcedApp()
                .withKafkaConfig(builder ->
                        builder
                                .withKafkaApplicationId("userMappedJsonApp1")
                                .withKafkaBootstrap("localhost:9092")
                                .build())
                .addAggregate(UserAggregate.createSpec(
                        aggregateName,
                        aggregateSerdes,
                        new PrefixResourceNamingStrategy("user_mapped_json_"),
                        (k) -> Optional.empty()
                ))
                .start()
                .getCommandAPISet("localhost");

        final CommandAPI<UserKey, UserCommand> api =
                commandApiSet.getCommandAPI(aggregateName);

        logger.info("Started publishing commands");
        final Result<CommandError, Sequence> result =
            submitCommands(api).unsafePerform(e -> CommandError.of(CommandError.Reason.InternalError, e));
        logger.info("Result of commands {}", result);
        logger.info("All commands published");
    }

}
