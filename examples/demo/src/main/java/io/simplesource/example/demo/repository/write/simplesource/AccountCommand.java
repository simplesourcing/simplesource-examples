package io.simplesource.example.demo.repository.write.simplesource;


import java.util.function.Function;

public abstract class AccountCommand {

    private AccountCommand() {}

    public abstract <T> T match(Function<CreateAccount, T> f1, Function<Deposit, T> f2, Function<Withdraw, T> f3);


    public final static class CreateAccount extends AccountCommand {
        public final String name;
        public final double openingBalance;

        public CreateAccount(String name, double openingBalance) {
            this.name = name;
            this.openingBalance = openingBalance;
        }

        @Override
        public <T> T match(Function<CreateAccount, T> f1, Function<Deposit, T> f2, Function<Withdraw, T> f3) {
            return f1.apply(this);
        }
    }

    public final static class Deposit extends AccountCommand {
        public final double amount;

        public Deposit(double amount) {
            this.amount = amount;
        }

        @Override
        public <T> T match(Function<CreateAccount, T> f1, Function<Deposit, T> f2, Function<Withdraw, T> f3) {
            return f2.apply(this);
        }
    }

    public final static class Withdraw extends AccountCommand {
        public final double amount;

        public Withdraw(double amount) {
            this.amount = amount;
        }

        @Override
        public <T> T match(Function<CreateAccount, T> f1, Function<Deposit, T> f2, Function<Withdraw, T> f3) {
            return f3.apply(this);
        }
    }

}

