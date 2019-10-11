package io.simplesource.example.demo.repository.write;

import io.simplesource.example.demo.domain.Account;

import java.util.Optional;

public interface AccountWriteRepository {
    Optional<CreateAccountError> create(Account account);
}
