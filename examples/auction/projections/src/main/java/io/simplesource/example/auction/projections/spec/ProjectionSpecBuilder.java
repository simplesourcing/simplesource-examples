package io.simplesource.example.auction.projections.spec;

import io.simplesource.kafka.model.ValueWithSequence;
import io.simplesource.kafka.serialization.avro.AvroGenericUtils;
import io.simplesource.kafka.serialization.util.GenericMapper;
import io.simplesource.kafka.serialization.util.GenericSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;

public final class ProjectionSpecBuilder<K, V> {

    private String schemaRegistryUrl;
    private boolean useMockSchemaRegistry;
    private String topicName;
    private GenericMapper<K, GenericRecord> projectionKeyMapper;
    private GenericMapper<V, GenericRecord> projectionValueMapper;

    private ProjectionSpecBuilder(String schemaRegistryUrl, boolean useMockSchemaRegistry) {
        this.schemaRegistryUrl = schemaRegistryUrl;
        this.useMockSchemaRegistry = useMockSchemaRegistry;
    }

    public static <K, V> ProjectionSpecBuilder<K, V> newBuilder(String schemaRegistryUrl, boolean useMockSchemaRegistry) {
        return new ProjectionSpecBuilder<>(schemaRegistryUrl, useMockSchemaRegistry);
    }

    public ProjectionSpecBuilder<K, V> withTopicName(final String topicName) {
        this.topicName = topicName;
        return this;
    }

    public ProjectionSpecBuilder<K, V> withKeyMapper(final GenericMapper<K, GenericRecord> keyMapper) {
        this.projectionKeyMapper = keyMapper;
        return this;
    }

    public ProjectionSpecBuilder<K, V> withValueMapper(final GenericMapper<V, GenericRecord> valueMapper) {
        this.projectionValueMapper = valueMapper;
        return this;
    }

    private Serde<GenericRecord> createSerde(boolean isKey) {
        return AvroGenericUtils.genericAvroSerde(schemaRegistryUrl, useMockSchemaRegistry, isKey,
                AvroGenericUtils.SchemaNameStrategy.TOPIC_NAME);
    }

    public ProjectionSpec<K, V> build() {
        Serde<K> keySerde = GenericSerde.of(createSerde(true), projectionKeyMapper);
        Serde<V> valueSerde = GenericSerde.of(createSerde(false), projectionValueMapper);

        return new ProjectionSpec<>(topicName, keySerde, valueSerde);
    }

    public ProjectionSpec<K, ValueWithSequence<V>> buildWithSequence() {
        Serde<K> keySerde = GenericSerde.of(createSerde(true), projectionKeyMapper);
        Serde<ValueWithSequence<V>> valueSerde = GenericSerde.of(createSerde(false),
                v -> AvroGenericUtils.ValueWithSequenceAvroHelper.toGenericRecord(v.map(projectionValueMapper::toGeneric)),
                s -> AvroGenericUtils.ValueWithSequenceAvroHelper.fromGenericRecord(s).map(projectionValueMapper::fromGeneric)
        );

        return new ProjectionSpec<>(topicName, keySerde, valueSerde);
    }
}
