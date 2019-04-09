package io.simplesource.example.auction.command;

import lombok.Value;

public abstract class AllocationCommand {
    private AllocationCommand() {}

    @Value
    public static final class Claim extends AllocationCommand {
    }

    @Value
    public static final class Release extends AllocationCommand {
    }
}
