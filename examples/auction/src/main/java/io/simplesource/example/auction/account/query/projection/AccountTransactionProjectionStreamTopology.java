package io.simplesource.example.auction.account.query.projection;

import com.google.common.collect.ImmutableList;
import io.simplesource.api.Aggregator;
import io.simplesource.example.auction.account.domain.AccountEvents;
import io.simplesource.example.auction.account.domain.AccountEvents.AccountEvent;
import io.simplesource.example.auction.account.domain.AccountEvents.AccountTransactionEvent;
import io.simplesource.example.auction.account.domain.AccountKey;
import io.simplesource.example.auction.account.domain.AccountTransactionKey;
import io.simplesource.example.auction.account.domain.Reservation;
import io.simplesource.kafka.model.ValueWithSequence;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableList.of;

public final class AccountTransactionProjectionStreamTopology implements KStreamProjectionTopology {
    private static final String STATE_STORE_NAME_SEPARATOR = "_";
    private static final ImmutableList<Class> ACCOUNT_TRANSACTION_EVENT_TYPES = of(AccountEvents.FundsReserved.class,
            AccountEvents.FundsReservationCancelled.class, AccountEvents.ReservationConfirmed.class);

    private final Supplier<Optional<Reservation>> projectionValueInitializer = Optional::empty;
    private final ProjectionSpec<AccountTransactionKey, Optional<Reservation>> projectionSpec;
    private Serde<Optional<Reservation>> writeValueSerde;
    private final Aggregator<AccountTransactionEvent, Optional<Reservation>> accountAggregator;

    private Serde<AccountTransactionKey> writeKeySerde;
    private Serde<ValueWithSequence<AccountEvent>> readValueSerde;

    public AccountTransactionProjectionStreamTopology(ProjectionSpec<AccountTransactionKey, Optional<Reservation>> projectionSpec,
                                                      Aggregator<AccountTransactionEvent, Optional<Reservation>> accountAggregator) {
        this.projectionSpec = projectionSpec;
        this.accountAggregator = accountAggregator;

        this.writeKeySerde = projectionSpec.serialization().writeKeySerde();
        this.writeValueSerde = projectionSpec.serialization().writeValueSerde();
        this.readValueSerde = projectionSpec.serialization().serdes().valueWithSequence();
    }

    @Override
    public void addTopology(final KStream<AccountKey, ValueWithSequence<AccountEvents.AccountEvent>> accountEventStream) {
        KGroupedStream<AccountTransactionKey, ValueWithSequence<AccountEvent>> accountTransactionEventStream = accountEventStream
                .filter((k, v) -> isAccountTransactionEvent(v.value()))
                .groupBy(this::accountTransactionKey, Serialized.with(writeKeySerde, readValueSerde));

        Materialized<AccountTransactionKey, Optional<Reservation>, KeyValueStore<Bytes, byte[]>> materialized = Materialized.
                <AccountTransactionKey, Optional<Reservation>, KeyValueStore<Bytes, byte[]>>as(
                        projectionSpec.sourceTopicName() + STATE_STORE_NAME_SEPARATOR + projectionSpec.outputTopicName())
                .withKeySerde(writeKeySerde)
                .withValueSerde(writeValueSerde);

        KTable<AccountTransactionKey, Optional<Reservation>> accountProjectionKTable = accountTransactionEventStream
                .aggregate(projectionValueInitializer::get,
                        (k, event, v) -> accountAggregator.applyEvent(v, (AccountTransactionEvent) event.value()),
                        materialized);

        accountProjectionKTable.toStream().to(projectionSpec.outputTopicName(),
                Produced.with(writeKeySerde, writeValueSerde));
    }

    private AccountTransactionKey accountTransactionKey(AccountKey k, ValueWithSequence<AccountEvent> v) {
        AccountEvents.AccountTransactionEvent accountTransactionEvent = (AccountEvents.AccountTransactionEvent) v.value();
        return new AccountTransactionKey(k, accountTransactionEvent.getReservationId());
    }

    private boolean isAccountTransactionEvent(AccountEvents.AccountEvent domainEvent) {
        return ACCOUNT_TRANSACTION_EVENT_TYPES.contains(domainEvent.getClass());
    }
}
