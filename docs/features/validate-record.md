---
feature_id: validate-record
title: Validate Record
updated: 2026-04-01
---

# Validate Record

## What it does
Validates a MARC record against the MARC specification and returns a list of validation issues without persisting any data. The response always uses HTTP `200`; the `validationResult` payload contains zero or more `issues`, each describing a tag, severity, definition type, help URL, and human-readable message.

## Why it exists
The quickMARC editor validates records on demand before the user submits a save, giving cataloguers immediate feedback on MARC structural and content errors without requiring a save attempt.

## Entry point(s)
| Method | Path | Description |
|--------|------|-------------|
| POST | /records-editor/validate | Validates a `validatableRecord` and returns `validationResult` |

## Business rules and constraints
- The MARC specification used for validation is resolved by `marcFormat` from the request body (BIBLIOGRAPHIC, HOLDINGS, or AUTHORITY).
- Validation is specification-guided: issues are produced by comparing the record's fields, indicators, and subfields against the specification fetched from the specification-storage service.
- Only issues with severity `ERROR` are returned; warnings and informational notices are suppressed.
- Default field values are populated before validation to ensure system-managed fields do not produce false errors.
- Each returned `ValidationIssue` includes a `helpUrl` pointing to the field's specification page when available in the fetched specification.
- For indicator-related error codes (`INVALID_INDICATOR`, `UNDEFINED_INDICATOR`), blank indicators are represented as `\` (backslash) in the message rather than `#`.

## Error behavior
- `200 OK` – always returned when validation completes, even if issues are found (issues are in the response body).
- `400 Bad Request` – malformed or missing request body.
- `500 Internal Server Error` – unexpected server-side failure.

## Caching
The MARC specification is cached per format and tenant using Caffeine (`specifications` cache, maximum 500 entries, 24-hour TTL). Cache entries are refreshed without a miss window when a `specification-storage.specification.updated` Kafka event is received — see [Specification Cache Refresh](specification-refresh.md).

## Configuration
| Variable | Purpose |
|----------|---------|
| `spring.cache.caffeine.spec` | Global Caffeine cache specification (default: `maximumSize=500,expireAfterAccess=3600s`) |
| `folio.cache.spec.specifications.ttl` | TTL for the `specifications` cache (default: `24h`) |
| `folio.cache.spec.specifications.maximum-size` | Maximum entries in the `specifications` cache (default: `500`) |

## Dependencies and interactions
- **mod-record-specifications** – provides the MARC field specification used to validate the record. The specification is consumed via the `SpecificationStorageClient`.

### Internal feature dependencies
- [Specification Cache Refresh](specification-refresh.md) – the MARC specification is served from the cache that this feature keeps current; stale cache entries are replaced in place without a miss window when the specification changes.
