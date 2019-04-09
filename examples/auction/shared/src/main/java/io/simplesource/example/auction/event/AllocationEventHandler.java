package io.simplesource.example.auction.event;

import io.simplesource.api.Aggregator;
import io.simplesource.dsl.AggregatorBuilder;

import java.util.Optional;

public final class AllocationEventHandler {
    private AllocationEventHandler() {
    }

    public static Aggregator<AllocationEvent, Optional<Boolean>> instance =
            AggregatorBuilder.<AllocationEvent, Optional<Boolean>>newBuilder()
                    .onEvent(AllocationEvent.Claimed.class, handleClaimed())
                    .onEvent(AllocationEvent.Released.class, handleReleased())
                    .build();

    private static Aggregator<AllocationEvent.Claimed, Optional<Boolean>> handleClaimed() {
        return (currentAggregate, event) ->
                Optional.of(true);
    }

    private static Aggregator<AllocationEvent.Released, Optional<Boolean>> handleReleased() {
        return (currentAggregate, event) ->
                Optional.empty();
    }
}
