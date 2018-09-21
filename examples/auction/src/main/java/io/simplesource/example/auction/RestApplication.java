package io.simplesource.example.auction;

import io.simplesource.api.CommandAPI;
import io.simplesource.api.CommandAPISet;
import io.simplesource.example.auction.account.avro.AccountAvroMappers;
import io.simplesource.example.auction.account.command.AccountMappedAggregate;
import io.simplesource.example.auction.account.domain.Account;
import io.simplesource.example.auction.account.domain.AccountCommand;
import io.simplesource.example.auction.account.domain.AccountEvents;
import io.simplesource.example.auction.account.domain.AccountKey;
import io.simplesource.example.auction.account.query.projection.AccountProjectionStreamApp;
import io.simplesource.example.auction.account.query.repository.AccountRepository;
import io.simplesource.example.auction.account.query.repository.AccountTransactionRepository;
import io.simplesource.example.auction.account.service.AccountReadService;
import io.simplesource.example.auction.account.service.AccountReadServiceImpl;
import io.simplesource.example.auction.account.service.AccountWriteService;
import io.simplesource.example.auction.account.service.AccountWriteServiceImpl;
import io.simplesource.kafka.api.ResourceNamingStrategy;
import io.simplesource.kafka.dsl.AggregateSetBuilder;
import io.simplesource.kafka.dsl.KafkaConfig;
import io.simplesource.kafka.internal.streams.PrefixResourceNamingStrategy;
import io.simplesource.kafka.spec.AggregateSpec;
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

import java.util.Optional;

@SpringBootApplication
public class RestApplication {
    private static final String SCHEMA_REGISTRY_URL = "http://schema_registry:8081";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String APPLICATION_SERVER = "localhost:1234";
    private static final String ACCOUNT_AGGREGATE_NAME = "account";

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
            AggregateSetBuilder aggregateSetBuilder = new AggregateSetBuilder();

            aggregateSetBuilder.withKafkaConfig(new KafkaConfig.Builder()
                    .withKafkaApplicationId("account_app")
                    .withKafkaBootstrap(BOOTSTRAP_SERVERS)
                    .withApplicationServer(APPLICATION_SERVER)
                    .build());

            AggregateSpec<AccountKey, AccountCommand, AccountEvents.AccountEvent, Optional<Account>> aggregateSpec =
                    AccountMappedAggregate.createSpec(
                            ACCOUNT_AGGREGATE_NAME,
                            AccountAvroMappers.createDomainSerializer(SCHEMA_REGISTRY_URL),
                            accountResourceNamingStrategy(),
                            (k) -> Optional.empty());

            aggregateSetBuilder.addAggregate(aggregateSpec);

            commandApiSet = aggregateSetBuilder.build();
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

    public static ResourceNamingStrategy accountResourceNamingStrategy() {
        return new PrefixResourceNamingStrategy("account_avro_");
    }

    public static void main(String[] args) {
        SpringApplication.run(RestApplication.class, args);
        new AccountProjectionStreamApp().start();
    }
}
