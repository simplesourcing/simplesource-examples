package io.simplesource.example.auction.avro;

import io.simplesource.example.auction.allocation.wire.*;
import io.simplesource.example.auction.command.AllocationCommand;
import io.simplesource.example.auction.domain.AllocationKey;
import io.simplesource.example.auction.event.AllocationEvent;
import io.simplesource.kafka.serialization.avro.mappers.DomainMapperBuilder;
import io.simplesource.kafka.serialization.avro.mappers.DomainMapperRegistry;
import io.simplesource.kafka.serialization.util.GenericMapper;
import org.apache.avro.generic.GenericRecord;

import java.util.Optional;

import static io.simplesource.kafka.serialization.avro.AvroSpecificGenericMapper.specificDomainMapper;

public class AllocationAvroMappers extends AvroSerdeFactory<AllocationKey, AllocationCommand, AllocationEvent, Optional<Boolean>> {
    public AllocationAvroMappers(String schemaRegistryUrl, boolean useMockSchemaRegistry) {
        super(Allocated.SCHEMA$, schemaRegistryUrl, useMockSchemaRegistry);
    }

    @Override
    public GenericMapper<AllocationKey, GenericRecord> buildKeyMapper() {
        return new GenericMapper<AllocationKey, GenericRecord>() {
            @Override
            public GenericRecord toGeneric(final AllocationKey value) {
                return io.simplesource.example.auction.allocation.wire.AllocationId.newBuilder()
                        .setId(value.asString())
                        .build();
            }

            @Override
            public AllocationKey fromGeneric(final GenericRecord serialized) {
                final GenericMapper<AllocationId, GenericRecord> mapper = specificDomainMapper();
                final io.simplesource.example.auction.allocation.wire.AllocationId allocationId = mapper.fromGeneric(serialized);
                return new AllocationKey(allocationId.getId());
            }
        };
    }

    @Override
    public GenericMapper<AllocationCommand, GenericRecord> buildCommandMapper() {
        return new DomainMapperBuilder(new DomainMapperRegistry())

                .mapperFor(AllocationCommand.Claim.class, Claim.class)
                .toSerialized(c -> new Claim())
                .fromSerialized(c -> new AllocationCommand.Claim())
                .register()

                .mapperFor(AllocationCommand.Release.class, Release.class)
                .toSerialized(c -> new Release())
                .fromSerialized(c -> new AllocationCommand.Release())
                .register()

                .withExceptionSupplierForNotRegisteredMapper(() -> new IllegalArgumentException("Command Class not supported"))
                .build();
    }

    @Override
    public GenericMapper<AllocationEvent, GenericRecord> buildEventMapper() {
        return new DomainMapperBuilder(new DomainMapperRegistry())

                .mapperFor(AllocationEvent.Claimed.class, Claimed.class)
                .toSerialized(c -> new Claimed())
                .fromSerialized(c -> new AllocationEvent.Claimed())
                .register()

                .mapperFor(AllocationEvent.Released.class, Released.class)
                .toSerialized(c -> new Released())
                .fromSerialized(c -> new AllocationEvent.Released())
                .register()

                .withExceptionSupplierForNotRegisteredMapper(() -> new IllegalArgumentException("Command Class not supported"))
                .build();
    }

    @Override
    public GenericMapper<Optional<Boolean>, GenericRecord> buildAggregateMapper() {
        return new GenericMapper<Optional<Boolean>, GenericRecord>() {

            @Override
            public GenericRecord toGeneric(Optional<Boolean> maybeAllocation) {
                return maybeAllocation.map(allocation -> new Allocated()).orElse(null);
            }

            @Override
            public Optional<Boolean> fromGeneric(GenericRecord serialized) {
                if (serialized == null) return Optional.empty();
                return Optional.of(true);
            }
        };
    }
}
