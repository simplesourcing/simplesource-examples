package io.simplesource.example.demo.web.viewobject;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;

public class AccountTransactionRow {
    private final Instant ts;
    private final double amount;

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public AccountTransactionRow(Instant ts, double amount) {
        this.ts = ts;
        this.amount = amount;
    }

    public String getFormattedTs() {
       return sdf.format(Date.from(ts));
    }

    public Instant getTs() {
        return ts;
    }

    public double getAmount() {
        return amount;
    }
}
