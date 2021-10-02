FROM folioci/alpine-jre-openjdk11:latest

# Copy your fat jar to the container
ENV APP_FILE mod-quick-marc-fat.jar
# - should be a single jar file
ARG JAR_FILE=./target/mod-quick-marc.jar
# - copy
COPY ${JAR_FILE} ${JAVA_APP_DIR}/${APP_FILE}

# Copy local lib to the container
ENV LIB_DIR ${VERTICLE_HOME}/lib
RUN mkdir -p ${LIB_DIR}
COPY lib/* ${LIB_DIR}/

# Expose this port locally in the container.
EXPOSE 8081
