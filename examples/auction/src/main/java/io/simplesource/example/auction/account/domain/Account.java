package io.simplesource.example.auction.account.domain;

import io.simplesource.example.auction.core.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public final class Account {
    @NonNull
    private final String username;
    @NonNull
    private Money funds;

    @Builder.Default
    private List<Reservation> fundReservations = Collections.emptyList();

    public Money availableFunds() {
        return funds.subtract(fundReservations.stream().map(r -> r.amount()).reduce(Money.ZERO, Money::add));
    }

    public Account addFunds(Money amount) {
        Objects.requireNonNull(amount);
        return new Account(this.username, this.funds.add(amount), fundReservations);
    }

    public Account subtractFunds(Money amount) {
        Objects.requireNonNull(amount);
        return new Account(this.username, this.funds.subtract(amount), fundReservations);
    }
}
