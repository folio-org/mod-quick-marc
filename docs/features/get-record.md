---
feature_id: get-record
title: Get Record
updated: 2026-05-06
---

# Get Record

## What it does
Retrieves a MARC record in the quickMARC view format by the UUID of the associated external entity (Instance, Holdings, or Authority). The response includes derived identifiers (`parsedRecordId`, `parsedRecordDtoId`, `externalHrid`), version information (`sourceVersion`), discovery suppression flag, record state and last-update date, field-level protection flags, and — for Bibliographic records — authority link details populated from the entity-links service. The last editor's user information is also included when available.

## Why it exists
The quickMARC editor needs to load an existing MARC record for display and editing. Clients identify a record by its external entity ID (e.g., an Instance UUID) rather than the internal SRS record ID, so this endpoint bridges that gap and enriches the raw record with editor-specific metadata before returning it.

## Entry point(s)
| Method | Path | Description |
|--------|------|-------------|
| GET | /records-editor/records | Returns a `quickMarcView` for the given external entity UUID |

## Business rules and constraints

#### Input validation
- **External ID:** `externalId` query parameter is required and must be a valid UUID.

#### Response enrichment
- **Field protection:** Flags are applied based on MARC field protection settings fetched from the data-import-converter-storage service; each field's `isProtected` property reflects the result.
- **Authority links:** Details (authority ID, natural ID, linking rule ID, link status, and error cause) are populated only for Bibliographic (`MARC_BIB`) records; Holdings and Authority records are returned without link data.
- **Last editor info:** The `updateInfo.updatedBy` field is populated only when the underlying SRS record carries a `metadata.updatedByUserId` and the Users service resolves that ID to a user.

#### MARC field handling
- **008 blank characters:** All blank characters in the MARC 008 fixed-length field (including leading ones at positions 00-05, the Date Entered on File) are preserved and represented as `\` in the quickMARC view. Fields are padded or truncated to their expected length using `\` for any missing positions.
- **Fixed-length field lengths:** The 008 field is normalised to **40 characters** for Bibliographic and Authority records, and to **32 characters** for Holdings records. Missing positions are padded with `\`; excess characters are truncated.

## Error behavior
- **400 Bad Request** – `externalId` query parameter is missing, not a valid UUID, or the request cannot be parsed.
- **404 Not Found** – no source record exists for the given `externalId`.
- **500 Internal Server Error** – unexpected server-side failure.

## Caching
Authority linking rules used during response enrichment are cached in the `linking-rules-results` Caffeine cache (maximum 500 entries, 1-hour access TTL) keyed by tenant ID. The record itself is not cached.

## Dependencies and interactions
- **mod-source-record-storage** – fetches the raw SRS record via `GET /source-storage/source-records?externalId=<id>&idType=EXTERNAL`.
- **data-import-converter-storage** – provides MARC field protection rules via `GET /field-protection-settings/marc?limit=1000`; applied before the record is returned.
- **mod-entities-links** (links service) – provides existing authority links for Bibliographic records via `GET /links/instances/{instanceId}`; also fetches linking rules used to populate link details.
- **mod-users** – resolves the `metadata.updatedByUserId` from the SRS record to a display name via `GET /users/{id}`.
