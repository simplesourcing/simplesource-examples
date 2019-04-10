package io.simplesource.example.auction.projections.spec;

import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.model.ValueWithSequence;
import lombok.Value;
import org.apache.kafka.common.serialization.Serde;

@Value
public class EventStreamSpec<K, E> {
    private final String topicName;
    private final Serde<K> keySerde;
    private final Serde<ValueWithSequence<E>> valueSerde;

    public static <K, E> EventStreamSpec<K, E> create(String topicName, AggregateSerdes<K, ?, E, ?> aggregateSerdes) {
        return new EventStreamSpec<>(topicName, aggregateSerdes.aggregateKey(), aggregateSerdes.valueWithSequence());
    }
}
