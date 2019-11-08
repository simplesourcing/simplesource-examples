package io.simplesource.example.auction.server.command;

import io.simplesource.api.CommandError;
import io.simplesource.api.CommandHandler;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Result;
import io.simplesource.example.auction.command.AllocationCommand;
import io.simplesource.example.auction.domain.AllocationKey;
import io.simplesource.example.auction.event.AllocationEvent;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AllocationCommandHandlerTest {
    private CommandHandler<AllocationKey, AllocationCommand, AllocationEvent, Optional<Boolean>> handler =
            AllocationCommandHandler.instance;

    private AllocationKey key = AllocationKey.of("Bob");

    @Test
    public void claimSuccess() {
        Result<CommandError, NonEmptyList<AllocationEvent>> result =
                handler.interpretCommand(key, Optional.empty(), new AllocationCommand.Claim());
        assertThat(result).isEqualTo(Result.success(NonEmptyList.of(new AllocationEvent.Claimed())));
    }

    @Test
    public void claimFailure() {
        Result<CommandError, NonEmptyList<AllocationEvent>> result =
                handler.interpretCommand(key, Optional.of(true), new AllocationCommand.Claim());
        assertThat(result).isEqualTo(Result.failure(NonEmptyList.of(new CommandError.InvalidCommand(
                "Bob is already taken"))));
    }

    @Test
    public void releaseSuccess() {
        Result<CommandError, NonEmptyList<AllocationEvent>> result =
                handler.interpretCommand(key, Optional.of(true), new AllocationCommand.Release());
        assertThat(result).isEqualTo(Result.success(NonEmptyList.of(new AllocationEvent.Released())));
    }

    @Test
    public void releaseSucceedsEvenIfNotClaimed() {
        Result<CommandError, NonEmptyList<AllocationEvent>> result =
                handler.interpretCommand(key, Optional.empty(), new AllocationCommand.Release());
        assertThat(result).isEqualTo(Result.success(NonEmptyList.of(new AllocationEvent.Released())));
    }
}
