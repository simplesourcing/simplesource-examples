package io.simplesource.example.user.avro;

import io.simplesource.api.CommandAPI;
import io.simplesource.api.CommandAPISet;
import io.simplesource.api.CommandError;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Result;
import io.simplesource.data.Sequence;
import io.simplesource.example.user.UserAggregate;
import io.simplesource.example.user.domain.User;
import io.simplesource.example.user.domain.UserCommand;
import io.simplesource.example.user.domain.UserEvent;
import io.simplesource.example.user.domain.UserKey;
import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.dsl.AggregateSetBuilder;
import io.simplesource.kafka.internal.streams.PrefixResourceNamingStrategy;
import io.simplesource.kafka.serialization.avro.AvroAggregateSerdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static io.simplesource.example.user.UserDomainCommandExample.submitCommands;
import static io.simplesource.example.user.avro.UserAvroMappers.*;

/**
 * This example demonstrates serializing with Avro but providing our own custom domain classes
 * for project, event command and key.
 *
 * @see UserAggregate
 */
public final class UserAvroRunner {
    private static final Logger logger = LoggerFactory.getLogger(UserAvroRunner.class);

    public static void main(final String[] args) {
        final AggregateSerdes<UserKey, UserCommand, UserEvent, Optional<User>> avroAggregateSerdes =
            new AvroAggregateSerdes<>(
                aggregateMapper, eventMapper, commandMapper, keyMapper,
                "http://localhost:8081",
                false,
                io.simplesource.example.user.avro.api.User.SCHEMA$);

        final String aggregateName = "example-user";
        final CommandAPISet aggregateSet = new AggregateSetBuilder()
            .withKafkaConfig(builder ->
                builder
                    .withKafkaApplicationId("userMappedAvroApp1")
                    .withKafkaBootstrap("localhost:9092")
                    .withApplicationServer("localhost:1234")
                    .build())
            .addAggregate(UserAggregate.createSpec(
                aggregateName,
                    avroAggregateSerdes,
                new PrefixResourceNamingStrategy("user_avro_"),
                (k) -> Optional.empty()
            ))
            .build();
        final CommandAPI<UserKey, UserCommand> api =
            aggregateSet.getCommandAPI(aggregateName);

        logger.info("Started publishing commands");
        final Result<CommandError, NonEmptyList<Sequence>> result =
            submitCommands(api).unsafePerform(e -> CommandError.of(CommandError.Reason.InternalError, e));
        logger.info("Result of commands {}", result);
        logger.info("All commands published");
    }

}