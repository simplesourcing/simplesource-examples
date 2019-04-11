package io.simplesource.example.auction.client.repository;

import io.simplesource.example.auction.client.views.AccountView;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(collectionResourceRel = "accounts", path = "accounts")
public interface AccountRepository extends MongoRepository<AccountView, String> {
    @Query("{ 'userName' : ?1} , {'id': {'$ne': ?0} }")
    List<AccountView> findOtherAccountsWithUsername(@Param("accountId") String accountId, @Param("username") String username);

    default Optional<AccountView> findByAccountId(String accountId) {
        return findById(accountId);
    }

    @Query("{ 'id': {'$in': ?0}}")
    List<AccountView> findByAccountIds(List<String> accountIds);
}
