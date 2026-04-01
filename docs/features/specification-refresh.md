---
feature_id: specification-refresh
title: Specification Cache Refresh
updated: 2026-04-01
---

# Specification Cache Refresh

## What it does
Listens for `specification-storage.specification.updated` Kafka events and refreshes the in-memory MARC specification cache entry for the affected tenant and profile. This ensures that subsequent validation, create, and update operations use the latest field rules without requiring a service restart.

## Why it exists
MARC specifications are cached to avoid repeated remote calls to the specification-storage service. When a specification is updated externally, the cache must be invalidated and reloaded so that validation reflects the new rules immediately.

## Entry point(s)
| Type | Topic | Description |
|------|-------|-------------|
| Kafka Consumer | `specification-storage.specification.updated` | Processes `SpecificationUpdatedEvent` messages and refreshes the specification cache |

### Event processing
- **When processed:** on each message, individually.
- **Event types handled:** `SpecificationUpdatedEvent` (carries `tenantId` and `specificationId`).
- **Processing behavior:**
  1. The tenant context is reconstructed from the Kafka message headers and the event's `tenantId` before any processing occurs.
  2. The full specification is fetched from the specification-storage service using the `specificationId` from the event.
  3. The fetched specification replaces the existing cache entry in the `specifications` cache under the key `{tenantId}:{profile}` (e.g., `diku:BIBLIOGRAPHIC`). Other format entries for the same or different tenants are not affected.
  4. If the `specifications` cache is not present in the cache manager (misconfiguration), the update is skipped and a warning is logged.

## Business rules and constraints
- Only the single specification identified by `specificationId` is refreshed; all other cache entries remain valid.
- Cache population uses `put` (not evict), so the first request after the event does not experience a cache miss.
- The listener operates with the concurrency configured via `KAFKA_EVENTS_CONCURRENCY` (default: `1`).

## Configuration
| Variable | Purpose |
|----------|---------|
| `folio.kafka.listener.specification-updated.topic-pattern` | Regex pattern for the consumed topic; defaults to `(${folio.environment}\.)(.*\.)specification-storage\.specification\.updated` |
| `folio.kafka.listener.specification-updated.group-id` | Consumer group ID; defaults to `${folio.environment}-mod-quick-marc-specification-group` |
| `KAFKA_EVENTS_CONCURRENCY` | Number of concurrent Kafka listener threads (default: `1`) |

## Dependencies and interactions
- **specification-storage** – the updated specification is fetched by `specificationId` from this service immediately upon receiving the event.
