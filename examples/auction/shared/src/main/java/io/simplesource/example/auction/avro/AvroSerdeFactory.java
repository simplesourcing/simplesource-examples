package io.simplesource.example.auction.avro;

import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.api.CommandSerdes;
import io.simplesource.kafka.serialization.avro.AvroAggregateSerdes;
import io.simplesource.kafka.serialization.avro.AvroCommandSerdes;
import io.simplesource.kafka.serialization.util.GenericMapper;
import org.apache.avro.Conversion;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificData;

import java.util.Collection;
import java.util.Collections;

/**
 * Template class for creating avro aggregate and command serdes.
 *
 * @param <K> key type.
 * @param <C> command type.
 * @param <E> event type.
 * @param <A> aggregate type.
 */
public abstract class AvroSerdeFactory<K, C, E, A> {
    private Schema schema;
    private String schemaRegistryUrl;
    private boolean useMockSchemaRegistry;

    protected AvroSerdeFactory(Schema schema, String schemaRegistryUrl, boolean useMockSchemaRegistry) {
        this.schema = schema;
        this.schemaRegistryUrl = schemaRegistryUrl;
        this.useMockSchemaRegistry = useMockSchemaRegistry;
    }

    /**
     * Create AggregateSerdes for a given domain object.
     *
     * @return AggregateSerdes.
     */
    public AggregateSerdes<K, C, E, A> createAggregateSerdes() {
        registerConversions();
        return new AvroAggregateSerdes<>(
                buildKeyMapper(), buildCommandMapper(), buildEventMapper(), buildAggregateMapper(),
                schemaRegistryUrl, useMockSchemaRegistry,
                schema);
    }

    /**
     * Create CommandSerdes for a given domain object.
     *
     * @return AggregateSerdes.
     */
    public CommandSerdes<K, C> createCommandSerdes() {
        registerConversions();
        return new AvroCommandSerdes<>(
                buildKeyMapper(), buildCommandMapper(), schemaRegistryUrl, useMockSchemaRegistry);
    }

    /**
     * If the schema requires any additional type conversions than those supported out of the box by avro
     * return them here.
     *
     * @return type conversions.
     */
    protected Collection<Conversion<?>> conversions() {
        return Collections.emptyList();
    }

    public void registerConversions() {
        conversions().forEach(c -> {
            SpecificData.get().addLogicalTypeConversion(c);
            GenericData.get().addLogicalTypeConversion(c);
        });
    }

    public abstract GenericMapper<K, GenericRecord> buildKeyMapper();

    public abstract GenericMapper<C, GenericRecord> buildCommandMapper();

    public abstract GenericMapper<E, GenericRecord> buildEventMapper();

    public abstract GenericMapper<A, GenericRecord> buildAggregateMapper();
}
