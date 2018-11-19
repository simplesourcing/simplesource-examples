package io.simplesource.example.auction.account.query.projection;

import io.simplesource.kafka.model.ValueWithSequence;
import io.simplesource.kafka.serialization.avro.AvroGenericUtils;
import io.simplesource.kafka.serialization.util.GenericMapper;
import io.simplesource.kafka.serialization.util.GenericSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;

import static io.simplesource.example.auction.account.query.projection.AccountProjectionStreamApp.ACCOUNT_AGGREGATE_SPEC;
import static io.simplesource.example.auction.AppShared.SCHEMA_REGISTRY_URL;
import static io.simplesource.kafka.serialization.avro.AvroGenericUtils.genericAvroSerde;

public final class AccountProjectionSpecBuilder {
    public static <K, V> ProjectionSpec<K, V> build(String sourceTopicName, String outputTopicName,
                                                    GenericMapper<K, GenericRecord> keyMapper,
                                                    GenericMapper<V, GenericRecord> valueMapper) {
        Serde<GenericRecord> projectionKeySerde = genericAvroSerde(SCHEMA_REGISTRY_URL, false, true, AvroGenericUtils.SchemaNameStrategy.TOPIC_NAME);
        Serde<GenericRecord> projectionValueSerde = genericAvroSerde(SCHEMA_REGISTRY_URL, false, false, AvroGenericUtils.SchemaNameStrategy.TOPIC_NAME);

        Serde<V> valueSerde = GenericSerde.of(projectionValueSerde, valueMapper);
        Serde<K> keySerde = GenericSerde.of(projectionKeySerde, keyMapper);

        ProjectionSpec.Serialization<K, V> serialization = new ProjectionSpec.Serialization<>(ACCOUNT_AGGREGATE_SPEC.serialization().serdes(),
                keySerde, valueSerde);

        return new ProjectionSpec<>(sourceTopicName, outputTopicName, serialization);
    }

    public static <K, V> ProjectionSpec<K, ValueWithSequence<V>> buildWithSequence(String sourceTopicName, String outputTopicName,
                                                                                   GenericMapper<K, GenericRecord> keyMapper,
                                                                                   GenericMapper<V, GenericRecord> valueMapper) {
        Serde<GenericRecord> projectionKeySerde = genericAvroSerde(SCHEMA_REGISTRY_URL, false, true, AvroGenericUtils.SchemaNameStrategy.TOPIC_NAME);
        Serde<GenericRecord> projectionValueSerde = genericAvroSerde(SCHEMA_REGISTRY_URL, false, false, AvroGenericUtils.SchemaNameStrategy.TOPIC_NAME);

        Serde<ValueWithSequence<V>> valueSerde = GenericSerde.of(projectionValueSerde,
                v -> AvroGenericUtils.ValueWithSequenceAvroHelper.toGenericRecord(v.map(valueMapper::toGeneric)),
                s -> AvroGenericUtils.ValueWithSequenceAvroHelper.fromGenericRecord(s).map(valueMapper::fromGeneric));

        Serde<K> keySerde = GenericSerde.of(projectionKeySerde, keyMapper);

        ProjectionSpec.Serialization<K, ValueWithSequence<V>> serialization = new ProjectionSpec.Serialization<>(ACCOUNT_AGGREGATE_SPEC.serialization().serdes(),
                keySerde, valueSerde);

        return new ProjectionSpec<>(sourceTopicName, outputTopicName, serialization);
    }
}
