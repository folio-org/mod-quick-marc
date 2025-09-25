# FOLIO mod-quick-marc Docker Compose Setup

This Docker Compose configuration provides a complete local development environment for the FOLIO mod-quick-marc module, including all necessary infrastructure components.

## 📋 Table of Contents

  * [🎯 Overview](#-overview)
  * [🚀 Quick Start](#-quick-start)
  * [⚙️ Configuration](#-configuration)
  * [🔧 Services](#-services)
  * [📖 Usage](#-usage)
  * [🛠️ Development](#-development)

## 🎯 Overview

This Docker Compose setup includes:

- **mod-quick-marc**: FOLIO module
- **PostgreSQL**: Database for module data persistence
- **pgAdmin**: Web-based database management tool
- **Apache Kafka**: Message broker for event-driven communication
- **Kafka UI**: Web interface for Kafka management
- **Kafka Topic Initializer**: Automated Kafka topic creation

## 🚀 Quick Start

1. **Review and adjust environment variables in .env file** (optional)

2. **Build and start all services**:
   ```bash
   docker compose -f app-docker-compose.yml up -d
   ```

3. **Verify all services are running**:
   ```bash
   docker compose -f app-docker-compose.yml ps
   ```

4. **Check module logs**:
   ```bash
   docker compose -f app-docker-compose.yml logs -f mod-quick-marc
   ```

## ⚙️ Configuration

### Environment Variables (.env)

| Variable                   | Default Value          | Description                           |
|----------------------------|------------------------|---------------------------------------|
| `COMPOSE_PROJECT_NAME`     | `folio-mod-quick-marc` | Docker Compose project name           |
| **Module Configuration**   |                        |                                       |
| `ENV`                      | `folio`                | Environment name                      |
| `MODULE_REPLICAS`          | `2`                    | Number of module instances to run     |
| **Database Configuration** |                        |                                       |
| `DB_HOST`                  | `postgres`             | PostgreSQL hostname                   |
| `DB_PORT`                  | `5432`                 | PostgreSQL port                       |
| `DB_DATABASE`              | `modules`              | Database name                         |
| `DB_USERNAME`              | `folio_admin`          | Database username                     |
| `DB_PASSWORD`              | `folio_admin`          | Database password                     |
| **pgAdmin Configuration**  |                        |                                       |
| `PGADMIN_DEFAULT_EMAIL`    | `user@domain.com`      | pgAdmin login email                   |
| `PGADMIN_DEFAULT_PASSWORD` | `admin`                | pgAdmin login password                |
| `PGADMIN_PORT`             | `5050`                 | pgAdmin web interface port            |
| **Kafka Configuration**    |                        |                                       |
| `KAFKA_HOST`               | `kafka`                | Kafka broker hostname                 |
| `KAFKA_PORT`               | `9093`                 | Kafka broker port                     |
| `KAFKA_TOPIC_PARTITIONS`   | `2`                    | Number of partitions for Kafka topics |
| `KAFKA_UI_PORT`            | `9000`                 | Kafka UI port                         |

## 🔧 Services

### mod-quick-marc
- **Purpose**: FOLIO module for quick MARC record editing
- **Access**: Dynamically assigned port (check with `docker compose ps`)
- **Scaling**: Configurable via `MODULE_REPLICAS`
- **Resource Limits**:
    - CPU: 0.5 cores (limit), 0.25 cores (reservation)
    - Memory: 512MB (limit), 256MB (reservation)

### PostgreSQL
- **Purpose**: Primary database for module data
- **Version**: PostgreSQL 16 Alpine
- **Access**: See `DB_PORT` in `.env`
- **Credentials**: See `DB_USERNAME` and `DB_PASSWORD` in `.env`

### pgAdmin
- **Purpose**: Database administration interface
- **Access**: See `PGADMIN_PORT` in `.env`
- **Login**: Use `PGADMIN_DEFAULT_EMAIL` and `PGADMIN_DEFAULT_PASSWORD` from `.env`

### Apache Kafka
- **Purpose**: Message broker for event-driven architecture
- **Mode**: KRaft (no Zookeeper required)
- **Listeners**:
    - Docker internal: `kafka:KAFKA_PORT`

### Kafka UI
- **Purpose**: Web interface for Kafka management
- **Access**: See `KAFKA_UI_PORT` in `.env`
- **Features**: Topic browsing, message viewing/producing, consumer group monitoring

## 📖 Usage

### Starting the Environment

```bash
# Start all services
docker compose -f app-docker-compose.yml up -d
```
```bash
# Start only infrastructure services
docker compose -f infra-docker-compose.yml up -d
```
```bash
# Start with build (if module code changed)
docker compose -f app-docker-compose.yml up -d --build
```
```bash
# Start specific service
docker compose -f app-docker-compose.yml up -d mod-quick-marc
```

### Stopping the Environment

```bash
# Stop all services
docker compose -f app-docker-compose.yml down
```
```bash
# Stop infra services
docker compose -f infra-docker-compose.yml down
```
```bash
# Stop and remove volumes (clean slate)
docker compose -f app-docker-compose.yml down -v
```

### Viewing Logs

```bash
# All services
docker compose -f app-docker-compose.yml logs
```
```bash
# Specific service
docker compose -f app-docker-compose.yml logs mod-quick-marc
```
```bash
# Follow logs
docker compose -f app-docker-compose.yml logs -f mod-quick-marc
```

### Scaling the Module

```bash
# Scale to 3 instances
docker compose -f app-docker-compose.yml up -d --scale mod-quick-marc=3

# Or modify MODULE_REPLICAS in .env and restart
```

### Cleanup and Reset

```bash
# Complete cleanup
docker compose -f app-docker-compose.yml down -v
```
```bash
# Recreate from scratch
docker compose -f app-docker-compose.yml up -d --build
```

## 🛠️ Development

### IntelliJ IDEA usage
Run ModQuickMarcApplication.java as a Spring Boot application with `dev` profile. 
It will automatically use `infra-docker-compose.yml` for starting infrastructure services.

### Building the Module
It's expected that the module is packaged to jar before building the Docker image. Use `mvn clean package` to build the jar.

```bash
# Build only the module image
docker compose -f app-docker-compose.yml build mod-quick-marc
```
```bash
# Build with no cache
docker compose -f app-docker-compose.yml build --no-cache mod-quick-marc
```

### Connecting to Services

```bash
# Connect to PostgreSQL
docker compose -f app-docker-compose.yml exec postgres psql -U folio_admin -d modules
```
```bash
# Access Kafka container
docker compose -f app-docker-compose.yml exec kafka bash
```
```bash
# Connect to module container
docker compose -f app-docker-compose.yml exec mod-quick-marc sh
```

### Adding New Kafka Topics

Edit `kafka-init.sh` and add topics to the `TOPICS` array:
```bash
TOPICS=(
  # ... existing topics ...
  "${ENV}.ALL.new-topic-name"
)
```