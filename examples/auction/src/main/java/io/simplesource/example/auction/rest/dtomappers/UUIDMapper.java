package io.simplesource.example.auction.rest.dtomappers;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public final class UUIDMapper {
    public String map(UUID id) {
        return Optional.ofNullable(id).map(UUID::toString).orElse(null);
    }
    public UUID map(String id) {
        return Optional.ofNullable(id).map(UUID::fromString).orElse(null);
    }
}
