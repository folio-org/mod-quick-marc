#!/bin/sh
DB_URL="jdbc:postgresql://${DB_HOST:-localhost}:${DB_PORT:-5432}/${DB_DATABASE:-db}"
KAFKA_URL="${KAFKA_HOST}:${KAFKA_PORT}"
OPTS="-Dspring.datasource.username=${DB_USERNAME:-user} -Dspring.datasource.password=${DB_PASSWORD:-pass} -Dspring.datasource.url=${DB_URL} -Dspring.kafka.bootstrap-servers=${KAFKA_URL}"
export JAVA_OPTIONS="${JAVA_OPTIONS:-} ${OPTS}"
