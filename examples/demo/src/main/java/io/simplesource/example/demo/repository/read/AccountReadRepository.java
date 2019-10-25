package io.simplesource.example.demo.repository.read;

import io.simplesource.example.demo.domain.AccountSummary;
import io.simplesource.example.demo.domain.AccountTransaction;

import java.util.List;
import java.util.Optional;

public interface AccountReadRepository {
    Optional<AccountSummary> accountSummary(String name);

    List<AccountSummary> list();

    List<AccountTransaction> getTransactions(String account);
}
