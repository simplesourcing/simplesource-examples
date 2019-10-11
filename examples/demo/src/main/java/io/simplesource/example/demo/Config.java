package io.simplesource.example.demo;

public final class Config {
    public final String kafkaGropId;
    public final String kafkaBootstrapServers;
    public final String elasticsearchHost;
    public final int elasticsearchPort;


    public Config(String kafkaGropId, String kafkaBootstrapServers, String elasticsearchHost, int elasticsearchPort) {
        this.kafkaGropId = kafkaGropId;
        this.kafkaBootstrapServers = kafkaBootstrapServers;
        this.elasticsearchHost = elasticsearchHost;
        this.elasticsearchPort = elasticsearchPort;
    }
}
