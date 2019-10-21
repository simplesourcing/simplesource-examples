package io.simplesource.example.demo.web.viewobject;

public class AccountListRow {
    public final String accountName;
    public final double balance;

    public AccountListRow(String accountName, double balance) {
        this.accountName = accountName;
        this.balance = balance;
    }
}
