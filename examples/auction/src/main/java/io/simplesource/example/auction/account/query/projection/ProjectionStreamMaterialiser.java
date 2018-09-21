package io.simplesource.example.auction.account.query.projection;

import org.apache.kafka.streams.kstream.KStream;

public interface ProjectionStreamMaterialiser<K, V> {
    void toTopic(KStream<K, V> stream, String topicName);
}
