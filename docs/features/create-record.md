---
feature_id: create-record
title: Create Record
updated: 2026-04-01
---

# Create Record

## What it does
Creates a brand-new MARC record together with its corresponding FOLIO inventory entity (Instance, Holdings, or Authority). The endpoint validates the submitted record, derives the inventory entity from the MARC content, persists both the inventory record and the SRS source record, and returns the full `quickMarcView` of the newly created record including the assigned external ID and HRID.

## Why it exists
Cataloguers need the ability to create original MARC records without importing from an external source. This endpoint provides a single, consistent entry point for creating all three MARC types within the quickMARC editor.

## Entry point(s)
| Method | Path | Description |
|--------|------|-------------|
| POST | /records-editor/records | Creates a new MARC record and its linked FOLIO entity |

## Business rules and constraints
- The MARC record is validated before creation. For Bibliographic and Authority formats, validation is run against the MARC specification with the `001 MISSING_FIELD` rule skipped (the 001 field is added automatically after the FOLIO entity is created). Holdings records skip specification-based validation.
- The inventory entity is created first; the resulting external UUID and HRID are stored in the new MARC record.
- Required MARC fields are added automatically after inventory creation:
  - A `999 $i` field containing the external entity UUID is added for all record types.
  - A `001` field containing the HRID is added for Instance and Holdings records.
- For Instance (Bibliographic) records, `003` fields are removed and `035` fields are normalised before the SRS record is created.
- The SRS record is created with `generation = 0`, `state = ACTUAL`, and `deleted = false`.

## Dependencies and interactions
- **mod-inventory-storage** (instance-storage, holdings-storage) – creates the Instance or Holdings inventory entity and returns the assigned ID and HRID.
- **mod-entities-links** (authority-storage) – creates the Authority inventory entity and returns the assigned ID.
- **mod-source-record-storage** – creates the new SRS record containing the raw and parsed MARC content.
- **mod-record-specifications** – MARC field specifications used for validation.

### Internal feature dependencies
- [Validate Record](validate-record.md) – validation logic is invoked for Bibliographic and Authority records before the inventory and SRS records are created; a validation failure results in `422` and nothing is persisted.
- [Specification Cache Refresh](specification-cache-refresh.md) – the MARC specification used during validation is served from the cache that this feature keeps current.

