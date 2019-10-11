package io.simplesource.example.demo.repository.write.simplesource;

import io.simplesource.api.Aggregator;

import java.util.Optional;

public final class AccountAggregator implements Aggregator<AccountEvent, Optional<Account>> {
    private static AccountAggregator instance = null;

    private AccountAggregator() {}

    public static AccountAggregator getInstance() {
        if(instance == null) {
            instance = new AccountAggregator();
        }

        return instance;
    }


    @Override
    public Optional<Account> applyEvent(Optional<Account> currentAggregate, AccountEvent event) {
        return event.match(
                accountCreated -> Optional.of(new Account(accountCreated.accountName, accountCreated.openingBalance)),
                deposited -> currentAggregate.map(account -> account.increment(deposited.amount)),
                withdrawn -> currentAggregate.map(account -> account.decrement(withdrawn.amount))
        );
    }
}
