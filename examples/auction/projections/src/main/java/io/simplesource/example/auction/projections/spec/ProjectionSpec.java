package io.simplesource.example.auction.projections.spec;

import lombok.Value;
import org.apache.kafka.common.serialization.Serde;

@Value
public final class ProjectionSpec<K, V> {
    private final String topicName;
    private final Serde<K> keySerde;
    private final Serde<V> valueSerde;
}
