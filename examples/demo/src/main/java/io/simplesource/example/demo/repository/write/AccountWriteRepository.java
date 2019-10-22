package io.simplesource.example.demo.repository.write;

import io.simplesource.data.Sequence;

import java.util.Optional;

public interface AccountWriteRepository {
    Optional<CreateAccountError> create(String accountName, double OpeningBalance);

    void deposit(String account, double amount, Sequence version);

    void withdraw(String account, double amount, Sequence position);
}
