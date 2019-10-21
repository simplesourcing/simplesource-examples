package io.simplesource.example.demo.web.form;

public class DepositForm {

    private double amount;

    public DepositForm(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
