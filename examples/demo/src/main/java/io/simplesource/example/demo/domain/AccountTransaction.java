package io.simplesource.example.demo.domain;

import java.time.Instant;

public class AccountTransaction {
    public final double amount;
    public final Instant ts;

    public AccountTransaction(double amount, Instant ts) {
        this.amount = amount;
        this.ts = ts;
    }
}
