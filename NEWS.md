## 1.2.0 - Unreleased

## 1.1.0 - Released

The only focus of this hotfix release was to update quickMARC record's edit status

[Full Changelog](https://github.com/folio-org/mod-quick-marc/compare/v1.0.0...v1.1.0)

### Stories
 * [MODQM-27](https://issues.folio.org/browse/MODQM-27) - quickMARC record's edit status

## 1.0.1 - Released

The only focus of this bug fix release was to fix bug in ParsedRecordDto to QuickMarcJson conversion 

[Full Changelog](https://github.com/folio-org/mod-quick-marc/compare/v1.0.0...v1.0.1)

### Bug Fixes
 * [MODQM-21](https://issues.folio.org/browse/MODQM-21) - Changing any field in Instance via quickMARC creates error for request to "records-editor/records/{id}"

## 1.0.0 - Released

The primary focus of this release was to implement backend logic for quickMARC - Simple MARC Records Editor

### Stories
 * [MODQM-20](https://issues.folio.org/browse/MODQM-20) - quickMARC: ParsedRecordDto -> QuickMarcJson error handling
 * [MODQM-19](https://issues.folio.org/browse/MODQM-19) - quickMARC response status to 202 on record update
 * [MODQM-18](https://issues.folio.org/browse/MODQM-18) - Update reference to data-import-raml-storage
 * [MODQM-17](https://issues.folio.org/browse/MODQM-17) - View source & quickMARC subfield formatting consistency
 * [MODQM-16](https://issues.folio.org/browse/MODQM-16) - Incorrect saved record fields order
 * [MODQM-15](https://issues.folio.org/browse/MODQM-15) - mod-quick-marc: Update to RMB v30
 * [MODQM-12](https://issues.folio.org/browse/MODQM-12) - quickMARC performance optimization
 * [MODQM-11](https://issues.folio.org/browse/MODQM-11) - marc-json <-> ui-compatible-marc-json converter updating (changing of control fields 006, 007)
 * [MODQM-10](https://issues.folio.org/browse/MODQM-10) - Update Record schema reference
 * [MODQM-9](https://issues.folio.org/browse/MODQM-9) - API Tests for GET /records-editor/records
 * [MODQM-8](https://issues.folio.org/browse/MODQM-8) - Integration with change-manager
 * [MODQM-6](https://issues.folio.org/browse/MODQM-6) - quickMARC performance tests
 * [MODQM-5](https://issues.folio.org/browse/MODQM-5) - Project Setup: mod-quick-marc
 * [MODQM-4](https://issues.folio.org/browse/MODQM-4) - Implement PUT records-editor/marc-records/id
 * [MODQM-3](https://issues.folio.org/browse/MODQM-3) - Implement GET records-editor/records endpoint
 * [MODQM-2](https://issues.folio.org/browse/MODQM-2) - marc-json <-> ui-compatible-marc-json converter
 * [MODQM-1](https://issues.folio.org/browse/MODQM-1) - UI-compatible json schema for MARC record
