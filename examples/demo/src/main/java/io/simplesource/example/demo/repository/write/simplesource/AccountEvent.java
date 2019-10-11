package io.simplesource.example.demo.repository.write.simplesource;

import java.util.function.Function;

public abstract class AccountEvent {

    private AccountEvent() {}

    public abstract  <T> T match(Function<AccountCreated, T> f1, Function<Deposited, T> f2, Function<Withdrawn, T> f3);

    public static final class AccountCreated extends AccountEvent {
        public final String accountName;
        public final double openingBalance;

        public AccountCreated(String accountName, double openingBalance) {
            this.accountName = accountName;
            this.openingBalance = openingBalance;
        }

        @Override
        public <T> T match(Function<AccountCreated, T> f1, Function<Deposited, T> f2, Function<Withdrawn, T> f3) {
            return f1.apply(this);
        }
    }

    public static final class Deposited extends AccountEvent {
        public final String account;
        public final double amount;

        public Deposited(String account, double amount) {
            this.account = account;
            this.amount = amount;
        }

        @Override
        public <T> T match(Function<AccountCreated, T> f1, Function<Deposited, T> f2, Function<Withdrawn, T> f3) {
            return f2.apply(this);
        }
    }

    public static final class Withdrawn extends AccountEvent {
        public final String account;
        public final double amount;

        public Withdrawn(String account, double amount) {
            this.account = account;
            this.amount = amount;
        }

        @Override
        public <T> T match(Function<AccountCreated, T> f1, Function<Deposited, T> f2, Function<Withdrawn, T> f3) {
            return f3.apply(this);
        }
    }

}
