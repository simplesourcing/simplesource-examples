package io.simplesource.example.auction.projections.account;

import io.simplesource.api.Aggregator;
import io.simplesource.example.auction.event.AccountEvent;
import io.simplesource.example.auction.event.AccountEvent.AccountTransactionEvent;
import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.example.auction.domain.AccountTransactionKey;
import io.simplesource.example.auction.domain.Reservation;
import io.simplesource.example.auction.projections.AbstractProjector;
import io.simplesource.example.auction.projections.spec.EventStreamSpec;
import io.simplesource.example.auction.projections.spec.ProjectionSpec;
import io.simplesource.kafka.model.ValueWithSequence;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Optional;

/**
 * Aggregates AccountTransactionEvents into Reservation to project onto a Kafka stream.
 */
public final class AccountTransactionProjector extends AbstractProjector<AccountKey, AccountEvent, AccountTransactionKey, Optional<Reservation>> {

    private final Aggregator<AccountTransactionEvent, Optional<Reservation>> aggregator = AccountTransactionEventAggregator.getAggregator();

    public AccountTransactionProjector(EventStreamSpec<AccountKey, AccountEvent> eventStreamSpec, ProjectionSpec<AccountTransactionKey, Optional<Reservation>> projectionSpec) {
        super(eventStreamSpec, projectionSpec);
    }

    @Override
    protected KStream<AccountTransactionKey, Optional<Reservation>> getProjectionKStream(
            final KStream<AccountKey, ValueWithSequence<AccountEvent>> eventStream,
            final Materialized<AccountTransactionKey, Optional<Reservation>, KeyValueStore<Bytes, byte[]>> materialized) {

        KGroupedStream<AccountTransactionKey, ValueWithSequence<AccountEvent>> accountTransactionEventStream =
                eventStream
                        .filter((k, v) -> v.value() instanceof AccountTransactionEvent)
                        .groupBy(this::accountTransactionKey, Serialized.with(getProjectionSpec().keySerde(), getEventStreamSpec().valueSerde()));
        KTable<AccountTransactionKey, Optional<Reservation>> accountProjectionKTable =
                accountTransactionEventStream
                        .aggregate(Optional::empty,
                                (k, event, v) -> aggregator.applyEvent(v, (AccountTransactionEvent) event.value()),
                                materialized);


        return accountProjectionKTable.toStream();
    }

    private AccountTransactionKey accountTransactionKey(AccountKey k, ValueWithSequence<AccountEvent> v) {
        AccountEvent.AccountTransactionEvent accountTransactionEvent = (AccountTransactionEvent) v.value();
        return new AccountTransactionKey(k, accountTransactionEvent.getReservationId());
    }
}
