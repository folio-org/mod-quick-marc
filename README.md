# mod-quick-marc

[![FOLIO](https://img.shields.io/badge/FOLIO-Module-blue)](https://www.folio.org/)
[![Release Version](https://img.shields.io/github/v/release/folio-org/mod-quick-marc?sort=semver&label=Latest%20Release)](https://github.com/folio-org/mod-quick-marc/releases)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=org.folio%3Amod-quick-marc&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=org.folio%3Amod-quick-marc)
[![Java Version](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/github/license/folio-org/mod-quick-marc)](LICENSE)

**Current Version:** `8.0.0-SNAPSHOT` (in development) | **Latest Stable:** `7.0.0` ([Release Notes](NEWS.md))

Copyright © 2020–2025 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0. See the file [LICENSE](LICENSE) for more information.

## Introduction
Spring Boot-based backend module that provides API for quickMARC — an in-app editor for MARC records in Source Record Storage (SRS). This module supports editing MARC bibliographic, holdings, and authority records.

<!-- TOC -->
* [mod-quick-marc](#mod-quick-marc)
  * [Introduction](#introduction)
  * [Technical Stack](#technical-stack)
  * [Configuration](#configuration)
    * [Environment Variables](#environment-variables)
      * [Database Configuration](#database-configuration)
      * [Kafka Configuration](#kafka-configuration)
      * [Application Configuration](#application-configuration)
      * [Kafka Topics](#kafka-topics)
    * [Server Configuration](#server-configuration)
    * [Management Endpoints](#management-endpoints)
  * [API](#api)
    * [Records Editor API (marc-records-editor v6.0)](#records-editor-api-marc-records-editor-v60)
    * [MARC Specifications API (marc-specifications v1.2)](#marc-specifications-api-marc-specifications-v12)
    * [API Documentation](#api-documentation)
    * [Required Permissions](#required-permissions)
    * [Validation rules in quickMARC](#validation-rules-in-quickmarc)
      * [MARC Holdings Validation rules](#marc-holdings-validation-rules)
      * [MARC Authority Validation rules](#marc-authority-validation-rules)
      * [MARC Bibliographic Validation rules](#marc-bibliographic-validation-rules)
  * [Related Modules](#related-modules)
    * [Core Dependencies](#core-dependencies)
  * [Development](#development)
    * [Prerequisites](#prerequisites)
    * [Building the Module](#building-the-module)
    * [Running Tests](#running-tests)
    * [Running Locally](#running-locally)
    * [Code Style](#code-style)
  * [Contributing](#contributing)
  * [Issue Tracker](#issue-tracker)
  * [Additional Documentation](#additional-documentation)
<!-- TOC -->

## Technical Stack

- **Java**: 21
- **Spring Boot**: 3.5.6
- **Build Tool**: Maven
- **Key Dependencies**:
    - PostgreSQL - Data persistence
    - Kafka - Event-driven communication

## Configuration

### Environment Variables

The module can be configured using the following environment variables:

#### Database Configuration

| Variable                | Default       | Description                           |
|-------------------------|---------------|---------------------------------------|
| `DB_HOST`               | `localhost`   | PostgreSQL database host              |
| `DB_PORT`               | `5432`        | PostgreSQL database port              |
| `DB_DATABASE`           | `modules`     | Database name                         |
| `DB_USERNAME`           | `folio_admin` | Database username                     |
| `DB_PASSWORD`           | `folio_admin` | Database password                     |
| `DB_MAXPOOLSIZE`        | `10`          | Maximum database connection pool size |
| `DB_CONNECTION_TIMEOUT` | `30000`       | Connection timeout in milliseconds    |
| `DB_IDLE_TIMEOUT`       | `600000`      | Idle timeout in milliseconds          |
| `DB_KEEPALIVE_TIME`     | `0`           | Keepalive time in milliseconds        |
| `DB_MAX_LIFETIME`       | `1800000`     | Maximum lifetime in milliseconds      |
| `DB_MINIMUM_IDLE`       | `10`          | Minimum idle connections              |

#### Kafka Configuration

| Variable                        | Default        | Description                                   |
|---------------------------------|----------------|-----------------------------------------------|
| `KAFKA_HOST`                    | `localhost`    | Kafka broker host                             |
| `KAFKA_PORT`                    | `9092`         | Kafka broker port                             |
| `KAFKA_SECURITY_PROTOCOL`       | `PLAINTEXT`    | Security protocol (PLAINTEXT, SSL, SASL_SSL)  |
| `KAFKA_SSL_KEYSTORE_LOCATION`   | -              | SSL keystore location (if SSL enabled)        |
| `KAFKA_SSL_KEYSTORE_PASSWORD`   | -              | SSL keystore password (if SSL enabled)        |
| `KAFKA_SSL_TRUSTSTORE_LOCATION` | -              | SSL truststore location (if SSL enabled)      |
| `KAFKA_SSL_TRUSTSTORE_PASSWORD` | -              | SSL truststore password (if SSL enabled)      |
| `NUMBER_OF_PARTITIONS`          | `5`            | Default number of partitions for Kafka topics |
| `REPLICATION_FACTOR`            | `1`            | Replication factor for Kafka topics           |
| `KAFKA_EVENTS_CONCURRENCY`      | `2`            | Consumer concurrency level                    |

#### Application Configuration

| Variable       | Default                     | Description                             |
|----------------|-----------------------------|-----------------------------------------|
| `ENV`          | `folio`                     | Environment name (used in topic naming) |

#### Kafka Topics

The module consumes from the following Kafka topics:
- `{ENV}.*.DI_COMPLETED` - Data import completion events
- `{ENV}.*.DI_ERROR` - Data import error events
- `{ENV}.*.QM_COMPLETED` - QuickMARC operation completion events
- `{ENV}.*.specification-storage.specification.updated` - MARC specification update events

### Server Configuration

| Variable      | Default | Description      |
|---------------|---------|------------------|
| `server.port` | `8081`  | HTTP server port |

### Management Endpoints

Management endpoints are available at `/admin`:
- `/admin/health` - Health check endpoint
- `/admin/info` - Application information
- `/admin/liquibase` - Database migration information
- `/admin/loggers` - Logger configuration
- `/admin/threaddump` - Thread dump
- `/admin/heapdump` - Heap dump

## API

### Records Editor API (marc-records-editor v6.0)

| Method | Endpoint                           | Parameters                                                                                                                                                                       | Permission                                  | Description                                                                         |
|--------|------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------|-------------------------------------------------------------------------------------|
| GET    | `/records-editor/records`          | **Query:**<br/>• `externalId` (UUID, required)                                                                                                                                   | `marc-records-editor.item.get`              | Retrieve a MARC record by external ID (instance, holdings, or authority)            |
| POST   | `/records-editor/records`          | **Body:** MARC record data                                                                                                                                                       | `marc-records-editor.item.post`             | Create a new MARC record and related entity (bibliographic, holdings, or authority) |
| PUT    | `/records-editor/records/{id}`     | **Path:**<br/>• `id` (UUID, record ID)<br/>**Body:** Updated MARC record data                                                                                                    | `marc-records-editor.item.put`              | Update an existing MARC record and related entity (async operation, returns 202)    |
| GET    | `/records-editor/records/status`   | **Query:**<br/>• `qmRecordId` (UUID, required)                                                                                                                                   | `marc-records-editor.status.item.get`       | Get the creation status of a MARC record                                            |
| POST   | `/records-editor/links/suggestion` | **Query:**<br/>• `authoritySearchParameter` (ID/NATURAL_ID, default: NATURAL_ID)<br/>• `ignoreAutoLinkingEnabled` (boolean, default: false)<br/>**Body:** MARC record to process | `marc-records-editor.links.suggestion.post` | Generate authority linking suggestions for a MARC bibliographic record              |
| POST   | `/records-editor/validate`         | **Body:** MARC record to validate                                                                                                                                                | `marc-records-editor.validate.post`         | Validate a MARC record without saving                                               |

### MARC Specifications API (marc-specifications v1.2)

| Method | Endpoint                                       | Parameters                                                                                                                 | Permission                     | Description                                    |
|--------|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|--------------------------------|------------------------------------------------|
| GET    | `/marc-specifications/{recordType}/{fieldTag}` | **Path:**<br/>• `recordType` (bibliographic, holdings, authority)<br/>• `fieldTag` (3-digit MARC tag, e.g., 008, 245, 852) | `marc-specifications.item.get` | Get MARC field specification for a record type |

### API Documentation
Complete API specifications with request/response schemas:
- [MARC Specifications API](https://s3.amazonaws.com/foliodocs/api/mod-quick-marc/s/marc-specifications.html)
- [Records Editor API](https://s3.amazonaws.com/foliodocs/api/mod-quick-marc/s/records-editor.html)
- [Records Editor Async API](https://s3.amazonaws.com/foliodocs/api/mod-quick-marc/s/records-editor-async.html)


### Required Permissions

Users need appropriate permissions to use the quickMARC API:

- **For all operations**: Use the aggregate permission `marc-records-editor.all`
- **For selective access**: Grant individual permissions as needed

_Note_: The module also requires various system-level permissions to interact with dependent services. These are automatically configured through the module descriptor.

### Validation rules in quickMARC
#### MARC Holdings Validation rules

| MARC field | Validation rule                    |
|:-----------|:-----------------------------------|
| **008**    | Required field  <br/> Unique field |    
| **852**    | Required field  <br/> Unique field |    

#### MARC Authority Validation rules

| MARC field | Validation rule                    |
|:-----------|:-----------------------------------|
| **1xx**    | Required field  <br/> Unique field |    
| **010**    | Optional field  <br/> Unique field |    

#### MARC Bibliographic Validation rules

| MARC field | Validation rule                    |
|:-----------|:-----------------------------------|
| **245**    | Required field  <br/> Unique field |    

## Related Modules

mod-quick-marc integrates with several other FOLIO modules to provide complete MARC record editing functionality:

### Core Dependencies

| Module                        | Repository                                                       | Description                                              |
|-------------------------------|------------------------------------------------------------------|----------------------------------------------------------|
| **mod-source-record-storage** | [GitHub](https://github.com/folio-org/mod-source-record-storage) | Source Record Storage - manages MARC records in SRS      |
| **mod-source-record-manager** | [GitHub](https://github.com/folio-org/mod-source-record-manager) | Orchestrates data import and record processing workflows |
| **mod-entities-links**        | [GitHub](https://github.com/folio-org/mod-entities-links)        | Handles authority-instance linking relationships         |
| **mod-record-specifications** | [GitHub](https://github.com/folio-org/mod-record-specifications) | Provides MARC specifications and validation rules        |

## Development

### Prerequisites
- Java 21
- Maven 3.6+
- Docker and Docker Compose (for local development)

### Building the Module
```bash
mvn clean install
```

### Running Tests
```bash
# Unit tests
mvn test

# Integration tests
mvn verify
```

### Running Locally

**Option 1: Using Spring Boot with dev profile (recommended for development)**
```bash
# Automatically starts PostgreSQL, Kafka, and other infrastructure via Docker Compose
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Option 2: Manual infrastructure setup**
```bash
# Start infrastructure services
docker compose -f docker/infra-docker-compose.yml up -d

# Run the module
mvn spring-boot:run
```

**Option 3: Full Docker setup**
```bash
# Build and run everything in Docker
docker compose -f docker/app-docker-compose.yml up -d

# Check logs
docker compose -f docker/app-docker-compose.yml logs -f mod-quick-marc
```

See [docker/README.md](docker/README.md) for detailed Docker Compose documentation.

### Code Style
The project uses Checkstyle to enforce code quality. Run:
```bash
mvn checkstyle:check
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines.

## Issue Tracker
See project [MODQM](https://folio-org.atlassian.net/jira/software/c/projects/MODQM/issues)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

## Additional Documentation
- Other [FOLIO modules](https://dev.folio.org/source-code/#server-side)
- FOLIO Developer documentation at [dev.folio.org](https://dev.folio.org/)
