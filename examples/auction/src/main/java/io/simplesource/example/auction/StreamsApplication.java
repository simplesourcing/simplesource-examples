package io.simplesource.example.auction;

import io.simplesource.example.auction.account.avro.AccountAvroMappers;
import io.simplesource.example.auction.aggregate.AccountAggregate;
import io.simplesource.kafka.dsl.EventSourcedApp;
import io.simplesource.kafka.dsl.KafkaConfig;
import org.apache.avro.Conversions;
import org.apache.avro.generic.GenericData;
import org.apache.avro.specific.SpecificData;

import java.util.Optional;

import static io.simplesource.example.auction.AppShared.*;

public class StreamsApplication {
    public static void main(final String[] args) {
        SpecificData.get().addLogicalTypeConversion(new Conversions.DecimalConversion());
        GenericData.get().addLogicalTypeConversion(new Conversions.DecimalConversion());
        new EventSourcedApp()
            .withKafkaConfig(new KafkaConfig.Builder()
                .withKafkaApplicationId("account_app")
                .withKafkaBootstrap(BOOTSTRAP_SERVERS)
            .build())
            .addAggregate(AccountAggregate.createSpec(
                    ACCOUNT_AGGREGATE_NAME,
                    AccountAvroMappers.createAggregateSerdes(SCHEMA_REGISTRY_URL),
                    accountResourceNamingStrategy(),
                    (k) -> Optional.empty()))
            .start();

    }
}


