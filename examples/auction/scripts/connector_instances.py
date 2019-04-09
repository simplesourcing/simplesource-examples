ACCOUNT_TOPIC_NAME = "auction_avro_account-aggregate"
ACCOUNT_CONNECT_INSTANCE_NAME = "Auction-Accounts-MongoDb-Sink"

ACCOUNT_TRANSACTIONS_TOPIC_NAME = "auction_account_transactions_projection"
ACCOUNT_TRANSACTIONS_CONNECT_INSTANCE_NAME = "Auction-Account-Transactions-MongoDb-Sink"

AUCTION_TOPIC_NAME = "auction_avro_auction-aggregate"
AUCTION_CONNECT_INSTANCE_NAME = "Auction-Auctions-MongoDb-Sink"

topic_connect_parameters = {
    ACCOUNT_TOPIC_NAME: {
        "mongodb.collection": "auction_account",
        "name": ACCOUNT_CONNECT_INSTANCE_NAME,
        "mongodb.document.id.strategy": "at.grahsl.kafka.connect.mongodb.processor.id.strategy.ProvidedInKeyStrategy",
        "transforms": "RenameId,RenameValue",
        "transforms.RenameId.type": "org.apache.kafka.connect.transforms.ReplaceField$Key",
        "transforms.RenameId.renames": "id:_id",
        "transforms.RenameValue.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
        "transforms.RenameValue.renames": "aggregate_update:value"
    },
    ACCOUNT_TRANSACTIONS_TOPIC_NAME: {
        "mongodb.collection": "auction_account_transactions",
        "name": ACCOUNT_TRANSACTIONS_CONNECT_INSTANCE_NAME,
        "mongodb.document.id.strategy": "at.grahsl.kafka.connect.mongodb.processor.id.strategy.FullKeyStrategy",
    },
    AUCTION_TOPIC_NAME: {
        "mongodb.collection": "auction_auction",
        "name": AUCTION_CONNECT_INSTANCE_NAME,
        "mongodb.document.id.strategy": "at.grahsl.kafka.connect.mongodb.processor.id.strategy.ProvidedInKeyStrategy",
        "transforms": "RenameId,RenameValue",
        "transforms.RenameId.type": "org.apache.kafka.connect.transforms.ReplaceField$Key",
        "transforms.RenameId.renames": "id:_id",
        "transforms.RenameValue.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
        "transforms.RenameValue.renames": "aggregate_update:value"
    }
}


def build_connect_config_dict(topic_name, custom_config):
    connect_instance_name = custom_config.get("name")
    connect_config = {
        "name": connect_instance_name,
        "config": {
            "connector.class": "at.grahsl.kafka.connect.mongodb.MongoDbSinkConnector",
            "name": connect_instance_name,
            "connector-name": connect_instance_name,
            "key.converter": "io.confluent.connect.avro.AvroConverter",
            "topics": topic_name,
            "value.converter": "io.confluent.connect.avro.AvroConverter",
            "mongodb.connection.uri": "mongodb://mongo:27017/auction_example?w=1&journal=true",
            "mongodb.max.num.retries": "5",
            "key.converter.schema.registry.url": "http://schema_registry:8081",
            "value.converter.schema.registry.url": "http://schema_registry:8081",
            "mongodb.delete.on.null.values" : "true"
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