package io.simplesource.example.demo.web.form;

public class WithdrawForm {

    private double amount;
    private long sequence;

    public WithdrawForm(double amount, long sequence) {
        this.amount = amount;
        this.sequence = sequence;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }
}
