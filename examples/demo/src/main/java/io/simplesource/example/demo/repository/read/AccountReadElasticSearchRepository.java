package io.simplesource.example.demo.repository.read;

import io.simplesource.example.demo.Config;
import io.simplesource.example.demo.domain.Account;
import io.simplesource.example.demo.domain.AccountSummary;
import io.simplesource.example.demo.domain.AccountTransaction;
import io.simplesource.example.demo.repository.read.AccountReadRepository;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.query.QuerySearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;


/**
 * Use Elasticsearch as our read datasource
 */
public class AccountReadElasticSearchRepository implements AccountReadRepository {
    private static final Logger log = LoggerFactory.getLogger(AccountReadElasticSearchRepository.class);

    private static final String SUMMARY_INDEX = "simplesourcedemo_account_summary";
    private static final String TRANSACTION_INDEX = "simplesourcedemo_account_transaction";


    private final RestHighLevelClient esClient;

    public AccountReadElasticSearchRepository(Config config) {
        esClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(config.elasticsearchHost, config.elasticsearchPort, "http")));
    }

    @Override
    public Optional<AccountSummary> accountSummary(String name) {
        GetRequest getRequest = new GetRequest(SUMMARY_INDEX, name);

        try{
            GetResponse getResponse = esClient.get(getRequest, RequestOptions.DEFAULT);
            if(getResponse.isExists()) {
                String account = (String) getResponse.getSourceAsMap().get("accountName");
                double balance = (double) getResponse.getSourceAsMap().get("balance");
                long version = Long.valueOf(getResponse.getSourceAsMap().get("sequence").toString());
                return Optional.of(new AccountSummary(account, balance, version));
            } else {
                return Optional.empty();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<AccountSummary> list() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(SUMMARY_INDEX);


       try {
           SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);

           ArrayList<AccountSummary> result = new ArrayList<>();

           Iterator<SearchHit> searchHits = searchResponse.getHits().iterator();
           SearchHit searchHit;
           while (searchHits.hasNext()) {
               searchHit = searchHits.next();
               String account = (String) searchHit.getSourceAsMap().get("accountName");
               double balance = (double) searchHit.getSourceAsMap().get("balance");
               long version = Long.valueOf(searchHit.getSourceAsMap().get("sequence").toString());
               result.add(new AccountSummary(account, balance, version));
           }

           return result;

       } catch (IOException e) {
           throw new RuntimeException("ElasticSearch query failure", e);
       }
    }

    @Override
    public List<AccountTransaction> getTransactions(String account) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(TRANSACTION_INDEX);

        QueryBuilder qb = QueryBuilders.termQuery("account", account);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(qb);

        searchRequest.source(searchSourceBuilder);

        try {
            SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);

            ArrayList<AccountTransaction> result = new ArrayList<>();

            Iterator<SearchHit> searchHits = searchResponse.getHits().iterator();
            SearchHit searchHit;

            while (searchHits.hasNext()) {
                searchHit = searchHits.next();
                double amount = (double) searchHit.getSourceAsMap().get("amount");
                Instant ts = Instant.parse((String) searchHit.getSourceAsMap().get("time"));
                result.add(new AccountTransaction(amount, ts));
            }

            return result;

        } catch (IOException e) {
            throw new RuntimeException("ElasticSearch query failure", e);
        }

    }

}
