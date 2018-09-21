package io.simplesource.example.auction.account.query.projection;

import io.simplesource.example.auction.account.domain.AccountEvents.AccountEvent;
import io.simplesource.example.auction.account.domain.AccountKey;
import io.simplesource.kafka.api.AggregateSerdes;
import lombok.Value;
import org.apache.kafka.common.serialization.Serde;

@Value
public final class ProjectionSpec<K, V> {
    private final String sourceTopicName;
    private final String outputTopicName;
    private final Serialization<K, V>  serialization;

    @Value
    public static class Serialization<K, V> {
        private final AggregateSerdes<AccountKey, ?, AccountEvent, ?> serdes;
        private final Serde<K> writeKeySerde;
        private final Serde<V> writeValueSerde;
    }
}
