package io.simplesource.example.demo.projections;

import io.simplesource.example.demo.repository.write.simplesource.AccountEvent;
import io.simplesource.kafka.model.ValueWithSequence;

public interface Indexer {
    void index(String key, ValueWithSequence<AccountEvent> value);
}
