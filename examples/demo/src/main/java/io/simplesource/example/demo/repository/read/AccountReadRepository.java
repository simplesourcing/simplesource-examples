package io.simplesource.example.demo.repository.read;

import io.simplesource.example.demo.domain.Account;

import java.util.Optional;

public interface AccountReadRepository {
    Optional<Account> findByName(String name);
}
