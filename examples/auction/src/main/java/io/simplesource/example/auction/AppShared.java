package io.simplesource.example.auction;

import io.simplesource.kafka.api.ResourceNamingStrategy;
import io.simplesource.kafka.util.PrefixResourceNamingStrategy;

public class AppShared {
    public static final String SCHEMA_REGISTRY_URL = "http://schema_registry:8081";
    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String ACCOUNT_AGGREGATE_NAME = "account";

    public static ResourceNamingStrategy accountResourceNamingStrategy() {
        return new PrefixResourceNamingStrategy("account_avro_");
    }
}
