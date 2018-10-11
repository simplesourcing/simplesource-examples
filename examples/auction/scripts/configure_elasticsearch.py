import requests
import time


def configure_elasticsearch_mappings():
    mapping_config = {
        "properties": {
            "username": {"type": "text"},
            "funds": {"type": "text"},
            "draftReservations": {
                "properties": {
                    "reservationId": {"type": "text"},
                    "description": {"type": "text"},
                    "amount": {"type": "text"},
                    "status": {"type": "text"}
                }
            },
            "sequence": {"type": "long"}
        }
    }

    while True:
        try:
            response = requests.put(
                'http://elasticsearch:9200/auction_account_projection/_mappings/simplesource-example-account/',
                json=mapping_config)
            mapping_config_response = valid_response(response)
            if mapping_config_response:
                print("Configure Elasticsearch with name %s passed with result %s" % (
                    mapping_config.get("name"), mapping_config_response))
                break
            print("Failed to configure the Elasticsearch mapping, got error code %s and response %s" % (
                response.status_code,
                response.text))

        except:
            print("Failed to connect to the Elasticsearch, Have you started Elasticsearch?")
        time.sleep(10)


def valid_response(response):
    valid_responsees = {200: "Connector configured successfully", 409: "Connector already exists"}
    return valid_responsees.get(response.status_code)


if __name__ == '__main__':
    configure_elasticsearch_mappings()
