import requests
import time
from connector_instances import AUCTION_EXAMPLE_CONNECT_INSTANCES


def configure_connect_instance(config):
    while True:
        try:
            response = requests.post('http://connect:8083/connectors', json=config)
            connector_config_response = valid_response(response)
            if connector_config_response:
                print("Configure Kafka Connect with name %s passed with result %s" % (
                config.get("name"), connector_config_response))
                break
            print("Failed to configure the connect instance, got error code %s and response %s" % (response.status_code,
                                                                                               response.text))

        except:
            print("Failed to connect to the Kafka connect, Have you started Kafka connect?")
        time.sleep(10)


def valid_response(response):
    valid_responsees = {201: "Connector configured successfully", 409: "Connector already exists"}
    return valid_responsees.get(response.status_code)

def configure_all():
    for config in AUCTION_EXAMPLE_CONNECT_INSTANCES:
        configure_connect_instance(config)

if __name__ == '__main__':
    configure_all()
