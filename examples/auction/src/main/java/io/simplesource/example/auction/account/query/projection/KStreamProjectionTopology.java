package io.simplesource.example.auction.account.query.projection;

import io.simplesource.example.auction.account.domain.AccountEvents;
import io.simplesource.example.auction.account.domain.AccountKey;
import io.simplesource.kafka.model.ValueWithSequence;
import org.apache.kafka.streams.kstream.KStream;

public interface KStreamProjectionTopology {
    void addTopology(final KStream<AccountKey, ValueWithSequence<AccountEvents.AccountEvent>> accountEventStream);
}
