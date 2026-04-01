# Module Features

This module provides the following features:

| Feature | Description |
|---------|-------------|
| [Get Record](features/get-record.md) | Retrieves a MARC record by external entity UUID, enriched with field protection flags and authority link details. |
| [Update Record](features/update-record.md) | Validates and persists edits to an existing MARC record in both Source Record Storage and the linked FOLIO inventory entity. |
| [Create Record](features/create-record.md) | Validates and creates a new MARC record together with its corresponding FOLIO inventory entity. |
| [Validate Record](features/validate-record.md) | Validates a MARC record against the MARC specification and returns a list of issues without saving. |
| [Links Suggestions](features/links-suggestions.md) | Analyses a Bibliographic MARC record and returns it enriched with suggested authority links for eligible fields. |
| [Specification Cache Refresh](features/specification-refresh.md) | Consumes `specification-storage.specification.updated` Kafka events to keep the in-memory MARC specification cache current. |
