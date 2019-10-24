package io.simplesource.example.demo.projections;

import io.simplesource.example.demo.repository.write.simplesource.AccountEvent;
import io.simplesource.kafka.model.ValueWithSequence;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class AccountTransactionProjection implements Indexer {
    private static final String INDEX = "simplesourcedemo_account_transaction";
    private static final Logger log = LoggerFactory.getLogger(AccountTransactionProjection.class);
    private final RestHighLevelClient esClient;

    public AccountTransactionProjection(RestHighLevelClient esClient) {
        this.esClient = esClient;
    }

    @Override
    public void index(String key, ValueWithSequence<AccountEvent> value) {
        value.value()
                .<Runnable>match(
                    accountCreated -> () -> insertTransaction(key, accountCreated.openingBalance, accountCreated.time, value.sequence().getSeq()),
                    deposited -> () -> insertTransaction(key, deposited.amount, deposited.time,value.sequence().getSeq()),
                    withdrawn -> () -> insertTransaction(key, -withdrawn.amount, withdrawn.time,value.sequence().getSeq())
                )
                .run();
    }

    public void insertTransaction(String account, double amount, Instant ts, long sequence) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("account", account);
            data.put("amount", amount);
            data.put("time", ts);

            IndexRequest req = new IndexRequest(INDEX)
                    .id(account + "_" + sequence)
                    .source(data);


            IndexResponse resp = esClient.index(req, RequestOptions.DEFAULT);
            log.info(account + " index status: " + resp.getResult().name());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
