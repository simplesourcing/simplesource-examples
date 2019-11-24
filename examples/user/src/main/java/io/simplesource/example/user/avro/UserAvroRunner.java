package io.simplesource.example.user.avro;

import io.simplesource.api.CommandAPI;
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
import io.simplesource.kafka.client.EventSourcedClient;
import io.simplesource.kafka.serialization.avro.AvroSerdes;
import io.simplesource.kafka.util.PrefixResourceNamingStrategy;
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

    private static PrefixResourceNamingStrategy namingStrategy = new PrefixResourceNamingStrategy("user_avro_");
    private static final String aggregateName = "example-user";
    private static final String bootstrapServers = "localhost:9092";
    private static final String schemaRegistry = "http://schema_registry:8081";

    public static void main(final String[] args) {

        startStreams();
        final CommandAPI<UserKey, UserCommand> api = startClient();

        // publish some commands
        logger.info("Started publishing commands");
        final Result<CommandError, Sequence> result =
                submitCommands(api).unsafePerform(e -> CommandError.of(CommandError.Reason.InternalError, e));
        logger.info("Result of commands {}", result);
        logger.info("All commands published");
    }

    private static void startStreams() {
        final AggregateSerdes<UserKey, UserCommand, UserEvent, Optional<User>> avroAggregateSerdes =
                AvroSerdes.Custom.aggregate(
                        keyMapper, commandMapper, eventMapper, aggregateMapper,
                        schemaRegistry,
                        false,
                        io.simplesource.example.user.avro.api.User.SCHEMA$);


        new EventSourcedApp()
                .withKafkaConfig(builder ->
                        builder
                                .withKafkaApplicationId("userMappedAvroApp1")
                                .withKafkaBootstrap(bootstrapServers)
                                .build())
                .withAggregate(UserAggregate.createSpec(
                        aggregateName,
                        avroAggregateSerdes,
                        namingStrategy,
                        (k) -> Optional.empty()
                ))
                .start();
    }

    private static CommandAPI<UserKey, UserCommand> startClient() {
        final CommandSerdes<UserKey, UserCommand> avroCommandSerdes =
                AvroSerdes.Custom.command(
                        keyMapper, commandMapper,
                        schemaRegistry,
                        false);

        final EventSourcedClient client =
                new EventSourcedClient().withKafkaConfig(builder -> builder.withKafkaBootstrap(bootstrapServers).build());

        return client.createCommandAPI(builder -> builder
                .withClientId("userAvroClient")
                .withName(aggregateName)
                .withSerdes(avroCommandSerdes)
                .withResourceNamingStrategy(namingStrategy)
                .build());
    }
}
