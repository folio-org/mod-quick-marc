#!/bin/bash

# Wait for Kafka to be ready
sleep 10

# Create Kafka topics
KAFKA_BROKER=${KAFKA_HOST}:${KAFKA_PORT}

echo "Creating Kafka topics..."

# Example topics
TOPICS=(
"${ENV}.ALL.specification-storage.specification.updated"
)

# Updated to use the full path to kafka-topics.sh
KAFKA_TOPICS_CMD="/opt/kafka/bin/kafka-topics.sh"

for TOPIC in "${TOPICS[@]}"; do
  $KAFKA_TOPICS_CMD \
    --create \
    --bootstrap-server "$KAFKA_BROKER" \
    --replication-factor 1 \
    --partitions "${KAFKA_TOPIC_PARTITIONS}" \
    --topic "$TOPIC"
  echo "Created topic: $TOPIC"
done

echo "Kafka topics created successfully."
