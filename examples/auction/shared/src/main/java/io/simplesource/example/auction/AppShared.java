package io.simplesource.example.auction;

import io.simplesource.kafka.api.ResourceNamingStrategy;
import io.simplesource.kafka.util.PrefixResourceNamingStrategy;

public class AppShared {
    public static final String SCHEMA_REGISTRY_URL = "http://schema_registry:8081";
    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String ACCOUNT_AGGREGATE_NAME = "account";
    public static final String AUCTION_AGGREGATE_NAME = "auction";

    // simple sagas
    public static final String SAGA_TOPIC_PREFIX = "saga_coordinator_";
    public static final String SAGA_BASE_NAME = "saga";
    public static final String ACTION_TOPIC_PREFIX  = "saga_action-";
    public static final String SAGA_ACTION_BASE_NAME = "saga_action";
    public static final String COMMAND_TOPIC_PREFIX = "auction_avro_";

    // naming strategy for event, command and aggregate topics
    public static ResourceNamingStrategy resourceNamingStrategy() {
        return new PrefixResourceNamingStrategy(COMMAND_TOPIC_PREFIX);
    }

    // to enforce username uniqueness
    public static final String USERNAME_ALLOCATION_AGGREGATE_NAME = "username_allocation";

    public static final int partitions = 6;
    public static final int replication = 1;
    public static final int retentionDays = 7;
}
