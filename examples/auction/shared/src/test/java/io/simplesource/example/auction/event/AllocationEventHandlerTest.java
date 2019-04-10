package io.simplesource.example.auction.event;

import io.simplesource.api.Aggregator;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AllocationEventHandlerTest {

    private Aggregator<AllocationEvent, Optional<Boolean>> handler = AllocationEventHandler.instance;

    @Test
    public void testClaimed() {
        assertThat(handler.applyEvent(Optional.empty(), new AllocationEvent.Claimed()))
                .isEqualTo(Optional.of(true));
    }

    @Test
    public void testReleased() {
        assertThat(handler.applyEvent(Optional.of(true), new AllocationEvent.Released()))
                .isEqualTo(Optional.empty());
    }

    @Test
    public void testReleasedNotClaimed() {
        assertThat(handler.applyEvent(Optional.empty(), new AllocationEvent.Released()))
                .isEqualTo(Optional.empty());
    }
}
