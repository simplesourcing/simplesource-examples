package io.simplesource.example.demo.repository.write.simplesource;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Account {
    @NotNull
    public final String name;

    @NotNull
    public final List<Transaction> transactions;

    public Account(String name, List<Transaction> transactions) {
        this.name = name;
        this.transactions = transactions;
    }

    public Account deposit(double amount) {
        List<Transaction> updated = new ArrayList<>(transactions);
        updated.add(new Transaction(amount, Instant.now()));
        return new Account(name, updated);
    }

    public Account withdraw(double amount) {
        List<Transaction> updated = new ArrayList<>(transactions);
        updated.add(new Transaction(-amount, Instant.now()));
        return new Account(name, updated);
    }

    public double balance() {
        return transactions.stream().collect(Collectors.summingDouble(transaction -> transaction.amount));
    }

    public static final class Transaction {
        public final double amount;
        public final Instant date;

        public Transaction(double amount, Instant date) {
            this.amount = amount;
            this.date = date;
        }
    }
}
