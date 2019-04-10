package io.simplesource.example.auction.client.repository;

import io.simplesource.example.auction.client.views.AccountTransactionView;
import io.simplesource.example.auction.client.views.AccountTransactionViewKey;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource(collectionResourceRel = "transactions", path = "transactions")
public interface AccountTransactionRepository extends MongoRepository<AccountTransactionView, AccountTransactionView.AccountTransactionId> {
    @Query("{'accountId': ?0 }")
    List<AccountTransactionView> findByAccountId(String accountId);

    default Optional<AccountTransactionView> findByTransactionKey(@NotNull AccountTransactionViewKey transactionKey) {
        return this.findById(new AccountTransactionView.AccountTransactionId(transactionKey.accountKey().asString(),
                transactionKey.reservationId().asString()));
    }
}
