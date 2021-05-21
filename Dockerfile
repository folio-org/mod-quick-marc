FROM folioci/alpine-jre-openjdk11:latest

# Copy your fat jar to the container
ENV APP_FILE mod-quick-marc-fat.jar
# - should be a single jar file
ARG JAR_FILE=./target/*.jar
# - copy
COPY ${JAR_FILE} ${JAVA_APP_DIR}/${APP_FILE}

ENV LIB_DIR ${JAVA_APP_DIR}/lib

RUN mkdir -p ${LIB_DIR}

COPY lib/mod-source-record-manager-client-3.1.0-SNAPSHOT.jar ${LIB_DIR}/

# Expose this port locally in the container.
EXPOSE 8081
