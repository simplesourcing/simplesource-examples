package io.simplesource.example.auction.account.query.projection;

import io.simplesource.api.Aggregator;
import io.simplesource.example.auction.account.domain.Account;
import io.simplesource.example.auction.account.event.AccountEvent;
import io.simplesource.example.auction.account.domain.AccountKey;
import io.simplesource.kafka.model.ValueWithSequence;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Optional;
import java.util.function.Supplier;

public final class AccountProjectionStreamTopology implements KStreamProjectionTopology {
    private static final String STATE_STORE_NAME_SEPARATOR = "_";

    private final Supplier<Optional<Account>> projectionValueInitializer = Optional::empty;
    private final ProjectionSpec<AccountKey, ValueWithSequence<Optional<Account>>> projectionSpec;
    private final Aggregator<AccountEvent, Optional<Account>> accountAggregator;

    private Serde<AccountKey> writeKeySerde;
    private Serde<ValueWithSequence<Optional<Account>>> writeValueSerde;
    private Serde<AccountKey> readKeySerde;
    private Serde<ValueWithSequence<AccountEvent>> readValueSerde;

    public AccountProjectionStreamTopology(ProjectionSpec<AccountKey, ValueWithSequence<Optional<Account>>> projectionSpec,
                                           Aggregator<AccountEvent, Optional<Account>> accountAggregator) {
        this.projectionSpec = projectionSpec;
        this.accountAggregator = accountAggregator;

        this.writeKeySerde = projectionSpec.serialization().writeKeySerde();
        this.writeValueSerde = projectionSpec.serialization().writeValueSerde();
        this.readKeySerde = projectionSpec.serialization().serdes().aggregateKey();
        this.readValueSerde = projectionSpec.serialization().serdes().valueWithSequence();
    }

    @Override
    public void addTopology(final KStream<AccountKey, ValueWithSequence<AccountEvent>> accountEventStream) {
        Materialized<AccountKey, ValueWithSequence<Optional<Account>>, KeyValueStore<Bytes, byte[]>> materialized = Materialized.
                <AccountKey, ValueWithSequence<Optional<Account>>, KeyValueStore<Bytes, byte[]>>as(
                        projectionSpec.sourceTopicName() + STATE_STORE_NAME_SEPARATOR + projectionSpec.outputTopicName())
                .withKeySerde(writeKeySerde)
                .withValueSerde(writeValueSerde);

        KTable<AccountKey, ValueWithSequence<Optional<Account>>> accountProjectionKTable =
                accountEventStream.groupByKey(Serialized.with(readKeySerde, readValueSerde))
                .aggregate(() -> ValueWithSequence.of(projectionValueInitializer.get()),
                        (k, event, v) -> new ValueWithSequence<>(accountAggregator.applyEvent(v.value(), event.value()),
                                event.sequence()),
                        materialized);

        accountProjectionKTable.toStream().to(projectionSpec.outputTopicName(),
                Produced.with(writeKeySerde, writeValueSerde));
    }
}
