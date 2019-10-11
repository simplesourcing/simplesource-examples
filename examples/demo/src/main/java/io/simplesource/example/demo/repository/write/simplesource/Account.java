package io.simplesource.example.demo.repository.write.simplesource;

import javax.validation.constraints.NotNull;

public class Account {
    @NotNull
    public final String name;

    public final double balance;

    public Account(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }


    public Account increment(double amount) {
        return new Account(name, balance + amount);
    }

    public Account decrement(double amount) {
        return new Account(name, balance - amount);
    }
}
