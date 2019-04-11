package io.simplesource.example.auction.server.aggregate;

import io.simplesource.api.InitialValue;
import io.simplesource.example.auction.command.AccountCommand;
import io.simplesource.example.auction.domain.Account;
import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.example.auction.event.AccountEvent;
import io.simplesource.example.auction.event.AccountEventHandler;
import io.simplesource.example.auction.server.command.AccountCommandHandler;
import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.api.ResourceNamingStrategy;
import io.simplesource.kafka.dsl.AggregateBuilder;
import io.simplesource.kafka.dsl.InvalidSequenceStrategy;
import io.simplesource.kafka.spec.AggregateSpec;

import java.util.Optional;

public final class AccountAggregate {

    public static AggregateSpec<AccountKey, AccountCommand, AccountEvent, Optional<Account>> createSpec(
            final String name,
            final AggregateSerdes<AccountKey, AccountCommand, AccountEvent, Optional<Account>> aggregateSerdes,
            final ResourceNamingStrategy resourceNamingStrategy,
            final InitialValue<AccountKey, Optional<Account>> initialValue
    ) {
        return AggregateBuilder.<AccountKey, AccountCommand, AccountEvent, Optional<Account>>newBuilder()
                .withName(name)
                .withSerdes(aggregateSerdes)
                .withInvalidSequenceStrategy(InvalidSequenceStrategy.Strict)
                .withResourceNamingStrategy(resourceNamingStrategy)
                .withInitialValue(initialValue)
                .withAggregator(AccountEventHandler.instance)
                .withCommandHandler(AccountCommandHandler.instance)
                .withDefaultTopicSpec(6, 1, 1)
                .build();
    }
}
