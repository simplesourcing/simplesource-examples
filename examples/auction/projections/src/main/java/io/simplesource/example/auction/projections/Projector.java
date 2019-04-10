package io.simplesource.example.auction.projections;

import io.simplesource.kafka.model.ValueWithSequence;
import org.apache.kafka.streams.kstream.KStream;

/**
 * A projector takes an event stream to set up different views of the current state.
 * @param <K>
 * @param <E>
 */
public interface Projector<K, E> {

    /**
     * Set up the projection on the given event stream.
     * @param eventStream to wire up the projection onto.
     */
    void setupProjection(final KStream<K, ValueWithSequence<E>> eventStream);
}
