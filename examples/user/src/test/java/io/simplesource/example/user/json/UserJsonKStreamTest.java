package io.simplesource.example.user.json;

import io.simplesource.data.Sequence;
import io.simplesource.data.NonEmptyList;
import io.simplesource.example.user.UserAggregate;
import io.simplesource.example.user.domain.UserCommand;
import io.simplesource.example.user.domain.UserEvent;
import io.simplesource.example.user.domain.UserKey;
import io.simplesource.example.user.domain.User;
import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.dsl.KafkaConfig;
import io.simplesource.kafka.model.ValueWithSequence;
import io.simplesource.kafka.internal.streams.AggregateTestDriver;
import io.simplesource.kafka.internal.streams.AggregateTestHelper;
import io.simplesource.kafka.internal.streams.PrefixResourceNamingStrategy;
import io.simplesource.kafka.serialization.json.JsonAggregateSerdes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.simplesource.api.CommandAPI.CommandError.*;
import static io.simplesource.kafka.serialization.json.JsonGenericMapper.jsonDomainMapper;
import static io.simplesource.kafka.serialization.json.JsonOptionalGenericMapper.jsonOptionalDomainMapper;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserJsonKStreamTest {
    private AggregateTestDriver<UserKey, UserCommand, UserEvent, Optional<User>> testAPI;
    private AggregateTestHelper<UserKey, UserCommand, UserEvent, Optional<User>> testHelper;

    @BeforeEach
    void setup() {
        final AggregateSerdes<UserKey, UserCommand, UserEvent, Optional<User>> aggregateSerdes =
            new JsonAggregateSerdes<>(
                jsonOptionalDomainMapper(),
                jsonDomainMapper(),
                jsonDomainMapper(),
                jsonDomainMapper()
            );

        testAPI = new AggregateTestDriver<>(
            UserAggregate.createSpec(
                "user",
                    aggregateSerdes,
                new PrefixResourceNamingStrategy("user_mapped_json_"),
                k -> Optional.empty()),
            new KafkaConfig.Builder()
                .withKafkaApplicationId("testApp")
                .withApplicationServer("server:8888")
                .withKafkaBootstrap("0.0.0.0:9092")
                .withExactlyOnce()
                .build());
        testHelper = new AggregateTestHelper<>(testAPI);
    }

    @AfterEach
    void tearDown() {
        if (testAPI != null) {
            testAPI.close();
        }
    }

    @Test
    void standardUserWorkflow() {
        final UserKey key = new UserKey("user2345");
        final String firstName = "Bob";
        final String lastName = "Dubois";
        final String updatedFirstName = "Bobbette";
        final String updatedLastName = "Dubois III";
        final int yearOfBirth = 1991;

        testHelper.publishCommand(
            key,
            Sequence.first(),
            new UserCommand.InsertUser(firstName, lastName))
            .expecting(
                NonEmptyList.of(new UserEvent.UserInserted(firstName, lastName)),
                Optional.of(new User(firstName, lastName, null))
            )
            .thenPublish(
                new UserCommand.UpdateName(updatedFirstName, updatedLastName))
            .expecting(
                NonEmptyList.of(
                    new UserEvent.FirstNameUpdated(updatedFirstName),
                    new UserEvent.LastNameUpdated(updatedLastName)),
                Optional.of(new User(updatedFirstName, updatedLastName, null))
            )
            .thenPublish(
                new UserCommand.UpdateYearOfBirth(yearOfBirth))
            .expecting(
                NonEmptyList.of(new UserEvent.YearOfBirthUpdated(yearOfBirth)),
                Optional.of(new User(updatedFirstName, updatedLastName, yearOfBirth))
            )
            .thenPublish(
                new UserCommand.DeleteUser())
            .expecting(
                NonEmptyList.of(new UserEvent.UserDeleted()),
                Optional.empty()
            );

    }

    @Test
    void updateBeforeInsert() {
        final UserKey id = new UserKey("national1");
        final String firstName = "Barnady";
        final String lastName = "Joyce";

        testHelper.publishCommand(
            id,
            Sequence.first(),
            new UserCommand.UpdateName(firstName, lastName))
            .expectingFailure(NonEmptyList.of(InvalidCommand));
    }

    @Test
    void invalidSequenceOnInsert() {
        final UserKey id = new UserKey("national2");
        final String firstName = "Michael";
        final String lastName = "McCormack";

        testHelper.publishCommand(
            id,
            Sequence.position(666L),
            new UserCommand.InsertUser(firstName, lastName))
            .expectingFailure(NonEmptyList.of(InvalidReadSequence));
    }

    @Test
    void invalidSequenceIdOnUpdate() {
        final UserKey id = new UserKey("myuser");
        final String firstName = "Renee";
        final String lastName = "Renet";
        final String updatedFirstName = "Renette";
        final String updatedLastName = "Rented";

        testHelper.publishCommand(
            id,
            Sequence.first(),
            new UserCommand.InsertUser(firstName, lastName))
            .expecting(
                NonEmptyList.of(new UserEvent.UserInserted(firstName, lastName)),
                Optional.of(new User(firstName, lastName, null))
            )
            .thenPublish(update ->
                new ValueWithSequence<>(new UserCommand.UpdateName(updatedFirstName, updatedLastName), Sequence.first()))
            .expectingFailure(NonEmptyList.of(InvalidReadSequence));
    }

    @Test
    void invalidCommand() {
        final UserKey id = new UserKey("myuser");

        testHelper.publishCommand(
            id,
            Sequence.first(),
            new UserCommand.UnhandledCommand())
            .expectingFailure(NonEmptyList.of(InvalidCommand));
    }

    @Test
    void buggyCommandHandler() {
        final UserKey id = new UserKey("myuser");

        testHelper.publishCommand(
            id,
            Sequence.first(),
            new UserCommand.BuggyCommand(true, false))
            .expectingFailure(NonEmptyList.of(CommandHandlerFailed));
    }

    @Test
    void buggyEventHandler() {
        final UserKey id = new UserKey("myuser");

        assertThrows(UnsupportedOperationException.class, () ->
            testHelper.publishCommand(
                id,
                Sequence.first(),
                new UserCommand.BuggyCommand(false, true))
                .expectingFailure(NonEmptyList.of(InvalidCommand))
        );
    }

}
