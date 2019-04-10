package io.simplesource.example.auction.event;

import lombok.Value;

public abstract class AllocationEvent {

    private AllocationEvent() {
    }

    @Value
    public static final class Claimed extends AllocationEvent {
    }

    @Value
    public static final class Released extends AllocationEvent {
    }

}
