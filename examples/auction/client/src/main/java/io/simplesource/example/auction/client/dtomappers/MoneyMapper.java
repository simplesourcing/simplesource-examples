package io.simplesource.example.auction.client.dtomappers;

import io.simplesource.example.auction.core.Money;

import java.math.BigDecimal;
import java.util.Optional;

public final class MoneyMapper {
    Money map(BigDecimal amount) {
        return Optional.ofNullable(amount).map(Money::valueOf).orElse(null);
    }
    BigDecimal map(Money amount) {
        return Optional.ofNullable(amount).map(Money::getAmount).orElse(null);
    }
}
