package io.simplesource.example.demo.repository.read;

import io.simplesource.example.demo.domain.Account;
import io.simplesource.example.demo.repository.read.AccountReadRepository;

import java.util.Optional;


/**
 * Use Elasticsearch as our read datasource
 */
public class AccountReadElasticSearchRepository implements AccountReadRepository {

    @Override
    public Optional<Account> findByName(String name) {
        return Optional.empty();
    }

}
