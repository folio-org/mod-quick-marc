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

#### Pre-processing
- **Specification resolution:** The MARC specification used for validation is resolved by `marcFormat` from the request body (BIBLIOGRAPHIC, HOLDINGS, or AUTHORITY).
- **Default field population:** Default field values are populated before validation to ensure system-managed fields do not produce false errors.
- **Specification-guided:** Issues are produced by comparing the record's fields, indicators, and subfields against the specification fetched from the specification-storage service.

#### Format-specific rules
- **Bibliographic:** leader positions `05` (record status), `06` (record type), `07` (bibliographic level), `08` (control type), `18` (cataloging form), and `19` (resource record level) are validated. Fields `008` and `245` must each appear exactly once.
- **Holdings:** `001`, `004`, `008`, and `852` must each appear exactly once. Leader positions are validated against the Holdings leader rule.
- **Authority:** `008` must appear exactly once; exactly one `1XX` field is required, and `010` may appear at most once. Leader positions are validated against the Authority leader rule.
- **All formats:** each field must have exactly two indicators, each a single character; required and unique-tag constraints from the MARC specification are enforced.
- **Validation is format-scoped:** only rules that declare support for the record's `marcFormat` are executed.

#### Response format
- **Severity filter:** Only issues with severity `ERROR` are returned; warnings and informational notices are suppressed.
- **Help URLs:** Each returned `ValidationIssue` includes a `helpUrl` pointing to the field's specification page when available in the fetched specification.
- **Blank indicators:** For indicator-related error codes (`INVALID_INDICATOR`, `UNDEFINED_INDICATOR`), blank indicators are represented as `\` (backslash) in the message rather than `#`.

## Error behavior
- `200 OK` – always returned when validation completes, even if issues are found (issues are in the response body).
- `400 Bad Request` – malformed or unreadable request body.
- `500 Internal Server Error` – unexpected server-side failure.

## Caching
The MARC specification is cached in the `specifications` Caffeine cache, keyed by `{tenantId}:{marcFormat}` (e.g., `diku:BIBLIOGRAPHIC`). Cache entries are refreshed in place — without a miss window — when a `specification-storage.specification.updated` Kafka event is received (see [Specification Cache Refresh](specification-refresh.md)).

| Property | Value |
|----------|-------|
| `folio.cache.spec.specifications.maximum-size` | `500` (default) |
| `folio.cache.spec.specifications.ttl` | `24h` (default) |
| `spring.cache.caffeine.spec` | `maximumSize=500,expireAfterAccess=3600s` (global default for other caches) |

## Configuration (if applicable)
| Variable | Purpose |
|----------|---------|
| `folio.cache.spec.specifications.ttl` | TTL for the `specifications` cache (default: `24h`) |
| `folio.cache.spec.specifications.maximum-size` | Maximum entries in the `specifications` cache (default: `500`) |
| `folio.tenant.validation.enabled` | Enables tenant-level validation; must be `true` for specification-based validation to run (default: `true`) |

## Dependencies and interactions
- **mod-record-specifications** – provides the MARC field specification used to validate the record; consumed via `SpecificationStorageClient` and served from the `specifications` cache.

### Internal feature dependencies
- [Specification Cache Refresh](specification-refresh.md) – the MARC specification is served from the cache that this feature keeps current; stale entries are replaced in place when the specification changes.
