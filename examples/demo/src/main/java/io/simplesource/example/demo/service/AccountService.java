package io.simplesource.example.demo.service;

import io.simplesource.example.demo.repository.write.CreateAccountError;

import java.util.Optional;

public interface AccountService {
    boolean accountExists(String accountName);

    Optional<CreateAccountError> createAccount(String name, double openingBalance);

}
