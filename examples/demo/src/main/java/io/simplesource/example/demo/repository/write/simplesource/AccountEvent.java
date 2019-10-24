package io.simplesource.example.demo.repository.write.simplesource;

import java.time.Instant;
import java.util.function.Function;

public abstract class AccountEvent {

    private AccountEvent() {}

    public abstract  <T> T match(Function<AccountCreated, T> f1, Function<Deposited, T> f2, Function<Withdrawn, T> f3);

    public static final class AccountCreated extends AccountEvent {
        public final String accountName;
        public final double openingBalance;
        public final Instant time;

        public AccountCreated(String accountName, double openingBalance, Instant time) {
            this.accountName = accountName;
            this.openingBalance = openingBalance;
            this.time = time;
        }

        @Override
        public <T> T match(Function<AccountCreated, T> f1, Function<Deposited, T> f2, Function<Withdrawn, T> f3) {
            return f1.apply(this);
        }
    }

    public static final class Deposited extends AccountEvent {
        public final double amount;
        public final Instant time;

        public Deposited(double amount, Instant time) {
            this.amount = amount;
            this.time = time;
        }

        @Override
        public <T> T match(Function<AccountCreated, T> f1, Function<Deposited, T> f2, Function<Withdrawn, T> f3) {
            return f2.apply(this);
        }
    }

    public static final class Withdrawn extends AccountEvent {
        public final double amount;
        public final Instant time;

        public Withdrawn(double amount, Instant time) {
            this.amount = amount;
            this.time = time;
        }

        @Override
        public <T> T match(Function<AccountCreated, T> f1, Function<Deposited, T> f2, Function<Withdrawn, T> f3) {
            return f3.apply(this);
        }
    }

}
