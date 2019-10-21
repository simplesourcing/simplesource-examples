package io.simplesource.example.demo.repository.write;

import java.util.Optional;

public interface AccountWriteRepository {
    Optional<CreateAccountError> create(String accountName, double OpeningBalance);
}
