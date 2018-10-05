package io.simplesource.example.user;

import io.simplesource.api.InitialValue;
import io.simplesource.example.user.domain.UserCommand;
import io.simplesource.example.user.domain.UserEvent;
import io.simplesource.example.user.domain.UserKey;
import io.simplesource.example.user.domain.User;
import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.api.ResourceNamingStrategy;
import io.simplesource.kafka.dsl.AggregateBuilder;
import io.simplesource.kafka.dsl.InvalidSequenceStrategy;
import io.simplesource.kafka.spec.AggregateSpec;

import java.util.Optional;


public final class UserAggregate {

    static public AggregateSpec<UserKey, UserCommand, UserEvent, Optional<User>> createSpec(
            final String name,
            final AggregateSerdes<UserKey, UserCommand, UserEvent, Optional<User>> aggregateSerdes,
            final ResourceNamingStrategy resourceNamingStrategy,
            final InitialValue<UserKey, Optional<User>> initialValue
    ) {
        return AggregateBuilder.<UserKey, UserCommand, UserEvent, Optional<User>>newBuilder()
                .withName(name)
                .withSerdes(aggregateSerdes)
                .withResourceNamingStrategy(resourceNamingStrategy)
                .withInitialValue(initialValue)
                .withAggregator(UserEvent.getAggregator())
                .withCommandHandler(UserCommand.getCommandHandler())
                .withCommandSequenceStrategy(InvalidSequenceStrategy.Strict)
                .build();
    }

}
