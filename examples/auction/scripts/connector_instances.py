MONGODB_ACCOUNT_INSTANCE_NAME = "Auction-Accounts-MongoDb-Sink"
MONGODB_ACCOUNT_TRANSACTIONS_INSTANCE_NAME = "Auction-Account-Transactions-MongoDb-Sink"
ELASTICSEARCH_ACCOUNT_INSTANCE_NAME = "Auction-Accounts-Elasticsearch-Sink"

topic_connect_parameters = {
    MONGODB_ACCOUNT_INSTANCE_NAME: {
        "connector.class": "at.grahsl.kafka.connect.mongodb.MongoDbSinkConnector",
        "mongodb.collection": "auction_account",
        "mongodb.document.id.strategy": "at.grahsl.kafka.connect.mongodb.processor.id.strategy.ProvidedInKeyStrategy",
        "topics": "auction_account_projection",
        "mongodb.connection.uri": "mongodb://mongo:27017/auction_example?w=1&journal=true",
        "mongodb.max.num.retries": "5",
        "mongodb.delete.on.null.values": "true",
        "transforms": "RenameId",
        "transforms.RenameId.type": "org.apache.kafka.connect.transforms.ReplaceField$Key",
        "transforms.RenameId.renames": "id:_id",
    },
    MONGODB_ACCOUNT_TRANSACTIONS_INSTANCE_NAME: {
        "mongodb.collection": "auction_account_transactions",
        "mongodb.document.id.strategy": "at.grahsl.kafka.connect.mongodb.processor.id.strategy.FullKeyStrategy",
        "topics": "auction_account_transactions_projection",
        "mongodb.delete.on.null.values": "true",
        "connector.class": "at.grahsl.kafka.connect.mongodb.MongoDbSinkConnector",
        "mongodb.connection.uri": "mongodb://mongo:27017/auction_example?w=1&journal=true",
        "mongodb.max.num.retries": "5"
    },
    ELASTICSEARCH_ACCOUNT_INSTANCE_NAME: {
        "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
        "connection.url": "http://elasticsearch:9200",
        "topics": "auction_account_projection",
        "type.name": "simplesource-example-account",
        "transforms": "ExtractIdFromKey",
        "transforms.ExtractIdFromKey.type": "org.apache.kafka.connect.transforms.ExtractField$Key",
        "transforms.ExtractIdFromKey.field": "id"
    }
}


def build_connect_config_dict(connect_instance_name, custom_config):
    connect_config = {
        "name": connect_instance_name,
        "config": {
            "name": connect_instance_name,
            "connector-name": connect_instance_name,
            "key.converter": "io.confluent.connect.avro.AvroConverter",
            "value.converter": "io.confluent.connect.avro.AvroConverter",
            "key.converter.schema.registry.url": "http://schema_registry:8081",
            "value.converter.schema.registry.url": "http://schema_registry:8081"
        }
    }
    connect_config.get("config").update(custom_config)
    return connect_config


def load_connect_instance_configs():
    connect_instances = []
    for k, v in topic_connect_parameters.items():
        connect_config = build_connect_config_dict(k, v)
        connect_instances.append(connect_config)

    return connect_instances


AUCTION_EXAMPLE_CONNECT_INSTANCES = load_connect_instance_configs()