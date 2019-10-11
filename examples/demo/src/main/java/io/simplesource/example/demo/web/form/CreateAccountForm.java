package io.simplesource.example.demo.web.form;

public class CreateAccountForm {

    public final static CreateAccountForm EMPTY = new CreateAccountForm("", 0);

    private final String accountName;
    private final double accountBalance;

    public CreateAccountForm(String accountName, double accountBalance) {
        this.accountName = accountName;
        this.accountBalance = accountBalance;
    }

    public String getAccountName() {
        return accountName;
    }

    public double getAccountBalance() {
        return accountBalance;
    }
}
