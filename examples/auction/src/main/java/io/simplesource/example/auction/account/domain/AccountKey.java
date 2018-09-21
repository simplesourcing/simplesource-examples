package io.simplesource.example.auction.account.domain;

import lombok.ToString;
import lombok.Value;
import java.util.UUID;

@Value
@ToString(includeFieldNames = false)
public final class AccountKey {
    @ToString.Include
    private final UUID id;

    public static AccountKey of(String id) {
        return new AccountKey(id);
    }

    public AccountKey(UUID id) {
        this.id = id;
    }
    public AccountKey(String id) {
        this.id = UUID.fromString(id);
    }

    public String asString() {
        return id.toString();
    }
}
