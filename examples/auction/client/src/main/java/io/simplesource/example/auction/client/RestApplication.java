package io.simplesource.example.auction.client;

import io.simplesource.api.CommandAPI;
import io.simplesource.api.CommandAPISet;
import io.simplesource.example.auction.AppShared;
import io.simplesource.example.auction.avro.AccountAvroMappers;
import io.simplesource.example.auction.avro.AuctionAvroMappers;
import io.simplesource.example.auction.client.repository.AccountRepository;
import io.simplesource.example.auction.client.repository.AccountTransactionRepository;
import io.simplesource.example.auction.client.repository.AuctionRepository;
import io.simplesource.example.auction.client.service.*;
import io.simplesource.example.auction.command.AccountCommand;
import io.simplesource.example.auction.command.AuctionCommand;
import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.example.auction.domain.AuctionKey;
import io.simplesource.kafka.dsl.EventSourcedClient;
import io.simplesource.kafka.spec.TopicSpec;
import io.simplesource.saga.model.api.SagaAPI;
import io.simplesource.saga.client.api.SagaClientBuilder;
import io.simplesource.saga.model.config.StreamAppConfig;
import io.simplesource.saga.serialization.avro.AvroSerdes;
import io.simplesource.saga.shared.topics.TopicNamer;
import org.apache.avro.Conversions;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificData;
// import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
//import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.lang.NonNull;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static io.simplesource.example.auction.AppShared.*;

/**
 * A web application with rest endpoints for commands and queries.
 */
@SpringBootApplication
public class RestApplication {

    private CommandAPISet commandApiSet = null;

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
                            .withSerdes(new AccountAvroMappers(SCHEMA_REGISTRY_URL, false).createCommandSerdes())
                            .withResourceNamingStrategy(AppShared.resourceNamingStrategy())
                            .withTopicSpec(new TopicSpec(8, (short) 1, Collections.emptyMap()))
                            .build())
                    .<AuctionKey, AuctionCommand>addCommands(builder -> builder
                            .withClientId("client_id")
                            .withCommandResponseRetention(3600L)
                            .withName(AUCTION_AGGREGATE_NAME)
                            .withSerdes(new AuctionAvroMappers(SCHEMA_REGISTRY_URL, false).createCommandSerdes())
                            .withResourceNamingStrategy(AppShared.resourceNamingStrategy())
                            .withTopicSpec(new TopicSpec(8, (short) 1, Collections.emptyMap()))
                            .build());

            commandApiSet = client
                    .build();
        }
        return commandApiSet;
    }

    @Bean
    public SagaAPI<GenericRecord> sagaAPI() {
        SagaClientBuilder<GenericRecord> sagaClientBuilder = SagaClientBuilder.create(builder ->
                builder.withStreamAppConfig(StreamAppConfig.of("saga-app-1", BOOTSTRAP_SERVERS)));
        return sagaClientBuilder
                .withSerdes(AvroSerdes.Generic.sagaSerdes(SCHEMA_REGISTRY_URL, false))
                .withTopicConfig(builder ->
                        builder
                                .withTopicNamer(TopicNamer.forPrefix(SAGA_TOPIC_PREFIX, SAGA_BASE_NAME))
                                .withDefaultTopicSpec(6, 1, 7)
                )
                .withClientId("saga-client-1")
                .build();
    }

    @Bean
    public AccountWriteService accountWriteService(@Qualifier("commandApiSet") CommandAPISet commandApiSet,
                                                   SagaAPI<GenericRecord> sagaAPI,
                                                   AccountRepository accountRepository) {
        CommandAPI<AccountKey, AccountCommand> commandApi = commandApiSet.getCommandAPI(ACCOUNT_AGGREGATE_NAME);
        return new AccountWriteServiceImpl(commandApi, sagaAPI, accountRepository);
    }

    @Bean
    public AccountReadService accountReadService(AccountTransactionRepository accountTransactionRepository) {
        return new AccountReadServiceImpl(accountTransactionRepository);
    }

    @Bean
    public AccountAvroMappers accountAvroMappers() {
        return new AccountAvroMappers(SCHEMA_REGISTRY_URL, false);
    }

    @Bean
    public AuctionWriteService auctionWriteService(@Qualifier("commandApiSet") CommandAPISet commandApiSet,
                                                   SagaAPI<GenericRecord> sagaAPI,
                                                   AccountRepository accountRepository,
                                                   AuctionRepository auctionRepository) {
        CommandAPI<AuctionKey, AuctionCommand> commandApi = commandApiSet.getCommandAPI(AUCTION_AGGREGATE_NAME);
        return new AuctionWriteServiceImpl(commandApi, sagaAPI, accountRepository, auctionRepository);
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

//    @Bean
//    public MongoCustomConversions mongoCustomConversions() {
//        return new MongoCustomConversions(Arrays.asList(
//                new Decimal128BigDecimalConverter()
//        ));
//    }
//
//    @ReadingConverter
//    private static class Decimal128BigDecimalConverter implements Converter<Decimal128, BigDecimal> {
//
//        @Override
//        public BigDecimal convert(@NonNull Decimal128 source) {
//            return source.bigDecimalValue();
//        }
//    }

    public static void main(String[] args) {
        SpringApplication.run(RestApplication.class, args);
    }
}
