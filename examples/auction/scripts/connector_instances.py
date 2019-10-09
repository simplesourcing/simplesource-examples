ACCOUNT_TOPIC_NAME = "auction_avro_account-aggregate"
ACCOUNT_CONNECT_INSTANCE_NAME = "Auction-Accounts-ElasticSearch-Sink"

ACCOUNT_TRANSACTIONS_TOPIC_NAME = "auction_account_transactions_projection"
ACCOUNT_TRANSACTIONS_CONNECT_INSTANCE_NAME = "Auction-Account-Transactions-ElasticSearch-Sink"

AUCTION_TOPIC_NAME = "auction_avro_auction-aggregate"
AUCTION_CONNECT_INSTANCE_NAME = "Auction-Auctions-ElasticSearch-Sink"

topic_connect_parameters = {
    ACCOUNT_TOPIC_NAME: {
        "name": ACCOUNT_CONNECT_INSTANCE_NAME,
        "transforms": "RenameId,RenameValue",
        "transforms.RenameId.type": "org.apache.kafka.connect.transforms.ReplaceField$Key",
        "transforms.RenameId.renames": "id:_id",
        "transforms.RenameValue.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
        "transforms.RenameValue.renames": "aggregate_update:value"
    },
    ACCOUNT_TRANSACTIONS_TOPIC_NAME: {
        "name": ACCOUNT_TRANSACTIONS_CONNECT_INSTANCE_NAME,
    },
    AUCTION_TOPIC_NAME: {
        "name": AUCTION_CONNECT_INSTANCE_NAME,
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
            "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
            "name": connect_instance_name,
            "connector-name": connect_instance_name,
            "key.converter": "io.confluent.connect.avro.AvroConverter",
            "topics": topic_name,
            "value.converter": "io.confluent.connect.avro.AvroConverter",
            "connection.url": "http://elasticsearch:9200",
            "key.converter.schema.registry.url": "http://schema_registry:8081",
            "value.converter.schema.registry.url": "http://schema_registry:8081",
            "behavior.on.null.values" : "delete"
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
