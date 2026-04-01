---
feature_id: get-record
title: Get Record
updated: 2026-04-01
---

# Get Record

## What it does
Retrieves a MARC record in the quickMARC view format by the UUID of the associated external entity (Instance, Holdings, or Authority). The response includes field-level protection flags and, for Bibliographic records, authority link details populated from the entity-links service. The last editor's user information is also included when available.

## Why it exists
The quickMARC editor needs to load an existing MARC record for display and editing. Clients identify a record by its external entity ID (e.g., an Instance UUID) rather than the internal SRS record ID, so this endpoint bridges that gap and enriches the raw record with editor-specific metadata before returning it.

## Entry point(s)
| Method | Path | Description |
|--------|------|-------------|
| GET | /records-editor/records | Returns a `quickMarcView` for the given external entity UUID |

## Business rules and constraints
- `externalId` query parameter is required and must be a valid UUID.
- Field protection flags are applied based on MARC field protection settings fetched from the data-import-converter-storage service; each field's `isProtected` property reflects the result.
- Authority link details (authority ID, natural ID, linking rule ID, link status, and error cause) are populated only for Bibliographic (`MARC_BIB`) records; Holdings and Authority records are returned without link data.
- The `updateInfo.updatedBy` field is populated only when the underlying SRS record carries a `metadata.updatedByUserId` and the Users service resolves that ID to a user.

## Dependencies and interactions
- **mod-source-record-storage** – fetches the raw SRS record by external ID.
- **data-import-converter-storage** (field protection settings) – provides MARC field protection rules applied before the record is returned.
- **mod-entities-links** (links service) – provides existing authority links for Bibliographic records.
- **mod-users** – resolves the user UUID from SRS metadata to a display name.
