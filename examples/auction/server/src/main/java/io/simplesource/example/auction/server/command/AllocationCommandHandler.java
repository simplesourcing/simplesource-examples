package io.simplesource.example.auction.server.command;

import io.simplesource.api.CommandError;
import io.simplesource.api.CommandHandler;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Result;
import io.simplesource.dsl.CommandHandlerBuilder;
import io.simplesource.example.auction.command.AllocationCommand;
import io.simplesource.example.auction.domain.AllocationKey;
import io.simplesource.example.auction.event.AllocationEvent;

import java.util.Optional;

import static io.simplesource.data.NonEmptyList.of;

public final class AllocationCommandHandler {

    public static CommandHandler<AllocationKey, AllocationCommand, AllocationEvent, Optional<Boolean>> instance =
            CommandHandlerBuilder.<AllocationKey, AllocationCommand, AllocationEvent, Optional<Boolean>>newBuilder()
                    .onCommand(AllocationCommand.Claim.class, doClaim())
                    .onCommand(AllocationCommand.Release.class, doRelease())
                    .build();

    private static CommandHandler<AllocationKey, AllocationCommand.Claim, AllocationEvent, Optional<Boolean>> doClaim() {
        return (allocationKey, currentAggregate, command) -> currentAggregate
                .map(a -> failure(allocationKey.asString() + " is already taken"))
                .orElse(success(new AllocationEvent.Claimed()));
    }

    private static CommandHandler<AllocationKey, AllocationCommand.Release, AllocationEvent, Optional<Boolean>> doRelease() {
        return ((allocationKey, currentAggregate, command) -> success(new AllocationEvent.Released()));
    }

    private static Result<CommandError, NonEmptyList<AllocationEvent>> failure(final String message) {
        return Result.failure(new CommandError.InvalidCommand(message));
    }

    @SafeVarargs
    private static <Event extends AllocationEvent> Result<CommandError, NonEmptyList<AllocationEvent>> success(final Event event, final Event... events) {
        return Result.success(of(event, events));
    }
}
