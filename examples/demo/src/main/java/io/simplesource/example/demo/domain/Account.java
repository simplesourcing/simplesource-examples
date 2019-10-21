package io.simplesource.example.demo.domain;

import java.time.Instant;
import java.util.List;

public class Account {
    public final String name;
    public final List<Transaction> transactions;

    public Account(String name, List<Transaction> transactions) {
        this.name = name;
        this.transactions = transactions;
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
