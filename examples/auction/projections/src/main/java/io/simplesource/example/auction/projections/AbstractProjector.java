package io.simplesource.example.auction.projections;

import io.simplesource.example.auction.projections.spec.EventStreamSpec;
import io.simplesource.example.auction.projections.spec.ProjectionSpec;
import io.simplesource.kafka.model.ValueWithSequence;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 * Template method for adding projection KStreams to an event stream such that every projection in our app
 * materialises into state stores under the same conventions.
 * @param <EK> event key.
 * @param <EV> event value.
 * @param <PK> projection key.
 * @param <PV> projection value.
 */
public abstract class AbstractProjector<EK, EV, PK, PV> implements Projector<EK, EV> {

    private static final String STATE_STORE_NAME_SEPARATOR = "_";

    private final EventStreamSpec<EK, EV> eventStreamSpec;
    private final ProjectionSpec<PK, PV> projectionSpec;

    public AbstractProjector(final EventStreamSpec<EK, EV> eventStreamSpec, final ProjectionSpec<PK, PV> projectionSpec) {
        this.eventStreamSpec = eventStreamSpec;
        this.projectionSpec = projectionSpec;
    }

    @Override
    public final void setupProjection(KStream<EK, ValueWithSequence<EV>> eventStream) {
        String storeName = eventStreamSpec.topicName() + STATE_STORE_NAME_SEPARATOR + projectionSpec.topicName();
        Materialized<PK, PV, KeyValueStore<Bytes, byte[]>> materialized =
                Materialized.<PK, PV, KeyValueStore<Bytes, byte[]>>as(storeName)
                        .withKeySerde(projectionSpec.keySerde())
                        .withValueSerde(projectionSpec.valueSerde());
        getProjectionKStream(eventStream, materialized)
                .to(projectionSpec.topicName(), Produced.with(projectionSpec.keySerde(), projectionSpec.valueSerde()));
    }

    /**
     * Return a KStream of the projection, given the event stream.
     * @param eventStream event stream.
     * @param materialized to materialize a state store for the projection.
     * @return projection KStream.
     */
    protected abstract KStream<PK, PV> getProjectionKStream(
            final KStream<EK, ValueWithSequence<EV>> eventStream,
            final Materialized<PK, PV, KeyValueStore<Bytes, byte[]>> materialized);

    protected EventStreamSpec<EK, EV> getEventStreamSpec() {
        return eventStreamSpec;
    }

    protected ProjectionSpec<PK, PV> getProjectionSpec() {
        return projectionSpec;
    }
}
