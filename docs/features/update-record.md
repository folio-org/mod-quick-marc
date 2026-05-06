---
feature_id: update-record
title: Update Record
updated: 2026-05-06
---

# Update Record

## What it does
Accepts an edited MARC record and persists the changes by updating both the Source Record Storage (SRS) record and the corresponding FOLIO inventory record (Instance, Holdings, or Authority). A `202 Accepted` with an empty body is returned after the update completes. The SRS record's generation counter is incremented on each successful save.

## Why it exists
The quickMARC editor is the primary tool for cataloguers to correct or enrich MARC data. Changes must propagate to both the SRS (the bibliographic source of truth) and the linked inventory record to keep the catalogue consistent. Separating the write acknowledgement (202) from completion allows the UI to remain responsive.

## Entry point(s)
| Method | Path | Description |
|--------|------|-------------|
| PUT | /records-editor/records/{id} | Updates the MARC record identified by the parsed record UUID |

## Business rules and constraints

#### Input validation
- **ID consistency:** The `id` path parameter must equal `parsedRecordId` in the request body; a mismatch results in a `400` error.
- **Validation:** MARC records are validated against the MARC specification before saving. For Bibliographic and Authority formats, validation errors result in a `422` response with a `validationResult` payload listing issues by tag. Holdings records skip specification-based validation.

#### Concurrency
- **Optimistic locking:** An optimistic-locking check compares the `_version` (generation) supplied in the request against the stored generation; a mismatch results in a `409 Conflict`.

#### Data propagation
- **Authority links:** When updating a Bibliographic (Instance) record, authority links embedded in the fields are extracted and written back to the entity-links service after the SRS update.
- **MARC-type normalization:**
  - **Bibliographic (Instance):** `003` fields are removed and `035` fields are normalised before saving. If the leader position 05 (`LDR/05`) equals `d` (deleted), `staffSuppress` and `discoverySuppress` are set to `true` on the inventory record.
  - **Holdings:** `003` fields are removed before saving.
  - **Authority:** no additional field normalization is applied.
- **Inventory merge semantics:** Holdings and Instance MARC-to-FOLIO field mapping uses `SET_TO_NULL` semantics: fields derived from MARC that are absent in the incoming record will be set to `null` in the inventory record (non-mapped fields such as `acquisitionMethod`, `temporaryLocationId`, `statisticalCodeIds`, etc., are preserved unchanged).
- **Protected FOLIO fields:** The `id`, `instanceId` (Holdings), and `version` of the existing FOLIO record are always preserved from the stored record before merge to prevent accidental overwrites.

#### MARC field handling
- **008 Date Entered auto-fill:** The `Entered` sub-field (Date Entered on File, positions 00-05) is automatically set to the current date if the stored value is absent, all-blank (`\\\\\\\\\\\`), non-numeric, or the sentinel value `000000`. This ensures the field always carries a valid date after a save.

## Error behavior
- **400 Bad Request** – `id` path parameter does not match `parsedRecordId` in the request body, or the request cannot be parsed.
- **404 Not Found** – source record or FOLIO inventory record not found for the given identifier.
- **409 Conflict** – optimistic-locking version mismatch (`_version` in request differs from stored generation).
- **422 Unprocessable Content** – MARC validation failed; response includes a `validationResult` payload with per-field issues.
- **500 Internal Server Error** – unexpected server-side failure.

## Dependencies and interactions
- **mod-source-record-storage** – creates a snapshot then writes the updated SRS record on every save.
- **mod-inventory-storage** (instance-storage, holdings-storage) – updates the corresponding inventory entity.
- **mod-entities-links** (authority-storage) – updates the corresponding Authority inventory entity.
- **mod-entities-links** (links service, Bibliographic records only) – rewrites authority links after the MARC record is saved.
- **mod-inventory-storage** (preceding-succeeding-titles) – rewrites preceding/succeeding title links after an Instance record is saved.
- **mod-record-specifications** – MARC field specifications used for validation.

### Internal feature dependencies
- [Validate Record](validate-record.md) – validation logic is invoked for Bibliographic and Authority records before the update is applied; a validation failure results in `422` and the record is not saved.
- [Specification Cache Refresh](specification-refresh.md) – the MARC specification used during validation is served from the cache that this feature keeps current.
