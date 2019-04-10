package io.simplesource.example.auction.domain;

import lombok.ToString;
import lombok.Value;

/**
 * AllocationKey contains the value to allocate, eg. "Bob", "bob@example.com", "+61412000000"
 */
@Value
@ToString(includeFieldNames = false)
public final class AllocationKey {
    @ToString.Include
    private final String id;

    public static AllocationKey of(String id) {
        return new AllocationKey(id);
    }

    public String asString() {
        return id;
    }
}
