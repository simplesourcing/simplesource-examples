package io.simplesource.example.demo.domain;

public class AccountSummary {
    public final String accountName;
    public final double balanace;

    public AccountSummary(String accountName, double balanace) {
        this.accountName = accountName;
        this.balanace = balanace;
    }
}
