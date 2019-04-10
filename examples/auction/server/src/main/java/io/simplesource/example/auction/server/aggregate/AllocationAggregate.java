package io.simplesource.example.auction.server.aggregate;

import io.simplesource.api.InitialValue;
import io.simplesource.example.auction.command.AllocationCommand;
import io.simplesource.example.auction.domain.AllocationKey;
import io.simplesource.example.auction.event.AllocationEvent;
import io.simplesource.example.auction.event.AllocationEventHandler;
import io.simplesource.example.auction.server.command.AllocationCommandHandler;
import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.api.ResourceNamingStrategy;
import io.simplesource.kafka.dsl.AggregateBuilder;
import io.simplesource.kafka.dsl.InvalidSequenceStrategy;
import io.simplesource.kafka.spec.AggregateSpec;

import java.util.Optional;

public class AllocationAggregate {

    public static AggregateSpec<AllocationKey, AllocationCommand, AllocationEvent, Optional<Boolean>> createSpec(
            final String name,
            final AggregateSerdes<AllocationKey, AllocationCommand, AllocationEvent, Optional<Boolean>> aggregateSerdes,
            final ResourceNamingStrategy resourceNamingStrategy,
            final InitialValue<AllocationKey, Optional<Boolean>> initialValue
    ) {
        return AggregateBuilder.<AllocationKey, AllocationCommand, AllocationEvent, Optional<Boolean>>newBuilder()
                .withName(name)
                .withSerdes(aggregateSerdes)
                .withInvalidSequenceStrategy(InvalidSequenceStrategy.LastWriteWins)
                .withResourceNamingStrategy(resourceNamingStrategy)
                .withInitialValue(initialValue)
                .withAggregator(AllocationEventHandler.instance)
                .withCommandHandler(AllocationCommandHandler.instance)
                .withDefaultTopicSpec(6, 1, 1)
                .build();
    }
}
