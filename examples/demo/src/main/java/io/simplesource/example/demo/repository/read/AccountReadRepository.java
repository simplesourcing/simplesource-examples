package io.simplesource.example.demo.repository.read;

import io.simplesource.example.demo.domain.Account;
import io.simplesource.example.demo.domain.AccountSummary;

import java.util.List;
import java.util.Optional;

public interface AccountReadRepository {
    boolean exists(String name);
    List<AccountSummary> list();
}
