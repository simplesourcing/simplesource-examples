package io.simplesource.example.demo.domain;

public class AccountSummary {
    public final String accountName;
    public final double balance;
    public final long version;

    public AccountSummary(String accountName, double balance, long version) {
        this.accountName = accountName;
        this.balance = balance;
        this.version = version;
    }
}
