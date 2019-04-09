#!/usr/bin/env bash

BROKER_CONTAINER_ID=$(docker ps -q -f "name=broker")

docker exec ${BROKER_CONTAINER_ID} kafka-console-consumer \
  --bootstrap-server broker:9092 \
  --property print.key=true \
  --from-beginning \
  --topic $@
