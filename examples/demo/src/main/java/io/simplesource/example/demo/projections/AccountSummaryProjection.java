package io.simplesource.example.demo.projections;

import io.simplesource.example.demo.repository.write.simplesource.AccountEvent;
import io.simplesource.kafka.model.ValueWithSequence;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AccountSummaryProjection implements Indexer {
    private static final String INDEX = "simplesourcedemo_account_summary";
    private static final Logger log = LoggerFactory.getLogger(AccountSummaryProjection.class);
    private final RestHighLevelClient esClient;


    public AccountSummaryProjection(RestHighLevelClient esClient) {
        this.esClient = esClient;
    }

    @Override
    public void index(String key, ValueWithSequence<AccountEvent> value) {
        value.value().match(
                accountCreated -> {
                    try {
                        Map<String, Object> data = new HashMap<>();
                        data.put("accountName", accountCreated.accountName);
                        data.put("balance", accountCreated.openingBalance);
                        data.put("sequence", value.sequence().getSeq());

                        IndexRequest req = new IndexRequest(INDEX)
                                .id(key)
                                .source(data);


                        IndexResponse resp = esClient.index(req, RequestOptions.DEFAULT);
                        log.info(key + " index status: " + resp.getResult().name());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }

                    return null;
                },
                deposited -> {
                    log.info("*****************{}**************", deposited.amount);
                    try {
                        // 1) Get current doc
                        GetRequest getRequest = new GetRequest(INDEX, key);
                        GetResponse getResponse = esClient.get(getRequest, RequestOptions.DEFAULT);

                        if(getResponse.isExists()) {
                            Map<String, Object> source = getResponse.getSource();
                            long sequence = Long.valueOf(source.get("sequence").toString()); // ES casts as a int so parse back as a long, hack should create an index schema

                            if(value.sequence().getSeq() == sequence+1) {
                                Map<String, Object> data = new HashMap<>();
                                data.put("balance",(double) source.get("balance") + deposited.amount);
                                data.put("sequence", value.sequence().getSeq());

                                UpdateRequest request = new UpdateRequest(INDEX, key)
                                        .doc(data);

                                UpdateResponse resp = esClient.update(request, RequestOptions.DEFAULT);
                                log.info("Updated AccountSummary projection with id {}, new sequence: {}. {}", key, value.sequence().getSeq(), resp);
                            } else {
                                log.error("Skipping deposited event projection with [{},{}]. Sequence miss-match", key, value.sequence().getSeq());
                            }

                        } else {
                            log.error("Skipping deposited event projection with [{},{}]. Existing entity not found", key, value.sequence().getSeq());
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                },
                withdrawn -> {
                    try {
                        // 1) Get current doc
                        GetRequest getRequest = new GetRequest(INDEX, key).version(value.sequence().getSeq() - 1);
                        GetResponse getResponse = esClient.get(getRequest, RequestOptions.DEFAULT);

                        // 2) Validate current doc version == sequenceNo - 1
                        if(getResponse.isExists()) {
                            Map<String, Object> source = getResponse.getSource();
                            long sequence = Long.valueOf(source.get("sequence").toString()); // ES casts as a int so parse back as a long, hack should create an index schema

                            if(value.sequence().getSeq() == sequence+1) {
                                Map<String, Object> data = new HashMap<>();
                                data.put("balance",(double) source.get("balance") - withdrawn.amount);
                                data.put("sequence", value.sequence().getSeq());

                                UpdateRequest request = new UpdateRequest(INDEX, key)
                                        .doc(data);

                                UpdateResponse resp = esClient.update(request, RequestOptions.DEFAULT);
                                log.info("Updated AccountSummary projection with id {}, new sequence: {}. {}", key, value.sequence().getSeq(), resp);
                            } else {
                                log.error("Skipping deposited event projection with [{},{}]. Sequence miss-match", key, value.sequence().getSeq());
                            }

                        } else {
                            log.error("Skipping deposited event projection with [{},{}]. Existing entity not found", key, value.sequence().getSeq());
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }
        );
    }
}
