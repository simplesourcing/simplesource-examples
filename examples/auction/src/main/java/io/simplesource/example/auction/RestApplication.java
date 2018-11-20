package io.simplesource.example.auction;

import io.simplesource.api.CommandAPI;
import io.simplesource.api.CommandAPISet;
import io.simplesource.example.auction.account.avro.AccountAvroMappers;
import io.simplesource.example.auction.account.domain.AccountCommand;
import io.simplesource.example.auction.account.domain.AccountKey;
import io.simplesource.example.auction.account.query.projection.AccountProjectionStreamApp;
import io.simplesource.example.auction.account.query.repository.AccountRepository;
import io.simplesource.example.auction.account.query.repository.AccountTransactionRepository;
import io.simplesource.example.auction.account.service.AccountReadService;
import io.simplesource.example.auction.account.service.AccountReadServiceImpl;
import io.simplesource.example.auction.account.service.AccountWriteService;
import io.simplesource.example.auction.account.service.AccountWriteServiceImpl;
import io.simplesource.kafka.dsl.EventSourcedClient;
import io.simplesource.kafka.dsl.KafkaConfig;
import io.simplesource.kafka.spec.TopicSpec;
import org.apache.avro.Conversions;
import org.apache.avro.generic.GenericData;
import org.apache.avro.specific.SpecificData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

import static io.simplesource.example.auction.AppShared.*;

@SpringBootApplication
public class RestApplication {

    private CommandAPISet commandApiSet = null;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountTransactionRepository accountTransactionRepository;

    @Bean
    public CommandAPISet commandApiSet() {
        SpecificData.get().addLogicalTypeConversion(new Conversions.DecimalConversion());
        GenericData.get().addLogicalTypeConversion(new Conversions.DecimalConversion());
        if (commandApiSet == null) {
            EventSourcedClient client = new EventSourcedClient();

            client.withKafkaConfig(builder -> builder
                    .withKafkaBootstrap(BOOTSTRAP_SERVERS)
                    .build())
                    .<AccountKey, AccountCommand>addCommands(builder -> builder
                        .withClientId("client_id")
                        .withCommandResponseRetention(3600L)
                        .withName(ACCOUNT_AGGREGATE_NAME)
                        .withSerdes(AccountAvroMappers.createCommandSerdes(SCHEMA_REGISTRY_URL))
                        .withResourceNamingStrategy(accountResourceNamingStrategy())
                        .withTopicSpec(new TopicSpec(8, (short)1, Collections.emptyMap()))
                        .build());

            commandApiSet = client
                    .build();
        }
        return commandApiSet;
    }

    @Bean
    public AccountWriteService accountWriteService(@Qualifier("commandApiSet") CommandAPISet commandApiSet) {
        CommandAPI<AccountKey, AccountCommand> commandApi = commandApiSet.getCommandAPI(ACCOUNT_AGGREGATE_NAME);
        return new AccountWriteServiceImpl(commandApi, accountRepository, accountTransactionRepository);
    }

    @Bean
    public AccountReadService accountReadService() {
        return new AccountReadServiceImpl(accountTransactionRepository);
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // you USUALLY want this
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    public static void main(String[] args) {
        SpringApplication.run(RestApplication.class, args);
        new AccountProjectionStreamApp().start();
    }
}
