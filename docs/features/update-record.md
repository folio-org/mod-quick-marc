---
feature_id: update-record
title: Update Record
updated: 2026-04-01
---

# Update Record

## What it does
Accepts an edited MARC record and persists the changes by updating both the Source Record Storage (SRS) record and the corresponding FOLIO inventory record (Instance, Holdings, or Authority). The operation is asynchronous from the caller's perspective: a `202 Accepted` is returned immediately after the update is enqueued, and the SRS record's generation counter is incremented on each successful save.

## Why it exists
The quickMARC editor is the primary tool for cataloguers to correct or enrich MARC data. Changes must propagate to both the SRS (the bibliographic source of truth) and the linked inventory record to keep the catalogue consistent. Separating the write acknowledgement (202) from completion allows the UI to remain responsive.

## Entry point(s)
| Method | Path | Description |
|--------|------|-------------|
| PUT | /records-editor/records/{id} | Updates the MARC record identified by the parsed record UUID |

## Business rules and constraints
- The `id` path parameter must equal `parsedRecordId` in the request body; a mismatch results in a `400` error.
- MARC records are validated against the MARC specification before saving. For Bibliographic and Authority formats, validation errors result in a `422` response with a `validationResult` payload listing issues by tag. Holdings records skip specification-based validation.
- An optimistic-locking check compares the `_version` (generation) supplied in the request against the stored generation; a mismatch results in a `409 Conflict`.
- When updating a Bibliographic (Instance) record, authority links embedded in the fields are extracted and written back to the entity-links service after the SRS update.
- Holdings and Instance MARC-to-FOLIO field mapping uses `SET_TO_NULL` merge semantics: fields derived from MARC that are absent in the incoming record will be set to `null` in the inventory record (non-mapped fields such as `acquisitionMethod`, `temporaryLocationId`, `statisticalCodeIds`, etc., are preserved unchanged).
- The `id`, `instanceId` (Holdings), and `version` of the existing FOLIO record are always preserved from the stored record before merge to prevent accidental overwrites.

## Dependencies and interactions
- **mod-source-record-storage** – reads the current SRS record for version check and writes the updated record.
- **mod-inventory-storage** (instance-storage, holdings-storage) – updates the corresponding inventory entity.
- **mod-entities-links** (authority-storage) – updates the corresponding Authority inventory entity.
- **mod-entities-links** (links service, Bibliographic records only) – rewrites authority links after the MARC record is saved.
- **mod-record-specifications** – MARC field specifications used for validation.

### Internal feature dependencies
- [Validate Record](validate-record.md) – validation logic is invoked for Bibliographic and Authority records before the update is applied; a validation failure results in `422` and the record is not saved.
- [Specification Cache Refresh](specification-refresh.md) – the MARC specification used during validation is served from the cache that this feature keeps current.
