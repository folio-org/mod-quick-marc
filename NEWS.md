## 2.5.0 IN-PROGRESS

* [MODQM-260] (https://issues.folio.org/browse/MODQM-260) - Supports users interface 15.0 16.0
* [MODQM-281] (https://issues.folio.org/browse/MODQM-281) - Remove 008 Desc from request/response on edit/derive MARC bib

## 2.4.0 - Released

### Changes
* [MODQM-195](https://issues.folio.org/browse/MODQM-195) - Delete Authority: Implement DELETE record
* [MODQM-203](https://issues.folio.org/browse/MODQM-203) - Moved to testcontainers
* [MODQM-204](https://issues.folio.org/browse/MODQM-204) - Change validation for holdings 852 tag
* [MODQM-206](https://issues.folio.org/browse/MODQM-206) - Refactor and improve record conversion
* [MODQM-207](https://issues.folio.org/browse/MODQM-207) - Extract externalRecordId from DI_COMPLETE after deletion
* [MODQM-209](https://issues.folio.org/browse/MODQM-209) - Fix validation error
* [MODQM-211](https://issues.folio.org/browse/MODQM-211) - Fix problem with cache for update action
* [MODQM-213](https://issues.folio.org/browse/MODQM-213) - MARC authority - Cannot Edit record (snapshot environment)
* [MODQM-218](https://issues.folio.org/browse/MODQM-218) - Fix OL handling and improve timeout handling
* [MODQM-223](https://issues.folio.org/browse/MODQM-223) - Migrate update to data-import flow
* [MODQM-225](https://issues.folio.org/browse/MODQM-225) - Improve handling of erroneous MARC bib Leader positions that cannot be edited via quickMARC
* [MODQM-226](https://issues.folio.org/browse/MODQM-226) - Improve handling of erroneous MARC holdings Leader positions that cannot be edited via quickMARC
* [MODQM-228](https://issues.folio.org/browse/MODQM-228) - added validation rule to check if only one 001 field is present in a record
* [MODQM-229](https://issues.folio.org/browse/MODQM-229) - Improve handling of erroneous MARC authority Leader positions that cannot be edited via quickMARC
* [MODQM-243](https://issues.folio.org/browse/MODQM-243) - Update folio-spring-base to v4.1.0

## 2.3.0 - Released

### Changes
* [MODQM-58](https://issues.folio.org/browse/MODQM-58) - Adjust the quickMarcJson payload to provide field protection status
* [MODQM-155](https://issues.folio.org/browse/MODQM-155) - MARC authority - 008 rules
* [MODQM-156](https://issues.folio.org/browse/MODQM-156) - Create a MARC Holdings Record
* [MODQM-159](https://issues.folio.org/browse/MODQM-159) - Support Authority record editing
* [MODQM-167](https://issues.folio.org/browse/MODQM-167) - Optimistic locking: mod-quick-marc modifications
* [MODQM-173](https://issues.folio.org/browse/MODQM-173) - Calculate leader values
* [MODQM-178](https://issues.folio.org/browse/MODQM-178) - Optimistic locking: Update return 409 if optimistic locking error
* [MODQM-179](https://issues.folio.org/browse/MODQM-179) - Remove ramls from the module
* [MODQM-179](https://issues.folio.org/browse/MODQM-179) - Remove ramls from the module
* [MODQM-181](https://issues.folio.org/browse/MODQM-181) - Log4j vulnerability verification and correction
* [MODQM-187](https://issues.folio.org/browse/MODQM-187) - Rewrite tests from RestAssured to Spring MockMVC
* [MODQM-188](https://issues.folio.org/browse/MODQM-188) - Create periodic job for cleanup database
* [MODQM-189](https://issues.folio.org/browse/MODQM-189) - Update folio-spring to v3.0
* [MODQM-197](https://issues.folio.org/browse/MODQM-197) - Fix Kafka configuration
* [MODQM-201](https://issues.folio.org/browse/MODQM-201) - Fix permissions on GET /record endpoint

## 2.2.0 - Released 

### Changes
* [MODQM-134](https://issues.folio.org/browse/MODQM-134) - Change dataType to have common type for MARC related subtypes
* [MODQM-127](https://issues.folio.org/browse/MODQM-127) - MARC record does NOT open after saving an invalid field
* [MODQM-138](https://issues.folio.org/browse/MODQM-138) - Update quickMarc PUT endpoint to handle asynchronous record update
* [MODQM-145](https://issues.folio.org/browse/MODQM-145) - QuickMarc not subscribe to QM_COMPLETED topic
* [MODQM-146](https://issues.folio.org/browse/MODQM-146) - View MARC Holdings via quickMARC
* [MODQM-142](https://issues.folio.org/browse/MODQM-142) - Backend: Edit MARC Holdings via quickMARC
* [MODQM-150](https://issues.folio.org/browse/MODQM-150) - MARC holdings - 008 rules
* [MODQM-151](https://issues.folio.org/browse/MODQM-151) - MARC holdings - Leader rules
* [MODQM-154](https://issues.folio.org/browse/MODQM-154) - MARC authority - Leader rules
* [MODQM-155](https://issues.folio.org/browse/MODQM-155) - MARC authority - 008 rules

## 2.1.0 - Released

### Changes
* [MODQM-96](https://issues.folio.org/browse/MODQM-96) - Add logging to controller and clients
* [MODQM-97](https://issues.folio.org/browse/MODQM-97) - Fix FeignException handler
* [MODQM-98](https://issues.folio.org/browse/MODQM-98) - Fetch user info while get record
* [MODQM-99](https://issues.folio.org/browse/MODQM-99) - Fix DB connections, not releasing while deriving record
* [MODQM-106](https://issues.folio.org/browse/MODQM-106) - Add category name to 007 field
* [MODQM-110](https://issues.folio.org/browse/MODQM-110) - Support MARC_BIB records instead of MARC
* [MODQM-124](https://issues.folio.org/browse/MODQM-124) - Fix derive without 001 field

## 2.0.5 - Released

### Changes
* [MODQM-107](https://issues.folio.org/browse/MODQM-107) - Support 006 field items
* [MODQM-122](https://issues.folio.org/browse/MODQM-122) - Fix handling of instance HRID

[Full Changelog](https://github.com/folio-org/mod-quick-marc/compare/v2.0.4...v2.0.5)

## 2.0.4 - Released

### Changes
* [MODQM-103](https://issues.folio.org/browse/MODQM-103) - Fix unexpected changing of 006 field
* [MODQM-98](https://issues.folio.org/browse/MODQM-98) - Fetch user info while get record

[Full Changelog](https://github.com/folio-org/mod-quick-marc/compare/v2.0.3...v2.0.4)


## 2.0.3 - Released

The only focus of this patch release was to add a standard admin healthcheck endpoint and remove Vert.X dependency

[Full Changelog](https://github.com/folio-org/mod-quick-marc/compare/v2.0.2...v2.0.3)

### Stories
* [MODQM-84](https://issues.folio.org/browse/MODQM-84) - Add standard admin healthcheck endpoint
* [MODQM-86](https://issues.folio.org/browse/MODQM-86) - Remove Vert.X dependency

## 2.0.2 - Released

The only focus of this patch release was to adjust POST payload according to the latest changes in 'mod-source-record-manager' 

[Full Changelog](https://github.com/folio-org/mod-quick-marc/compare/v2.0.1...v2.0.2)

### Stories
* [MODQM-89](https://issues.folio.org/browse/MODQM-89) - Update POST /jobExecutions/{jobExecutionId}/records payload with required fields

## 2.0.1 - Released

The only focus of this patch release was to fix a bug during duplication of MARC records

[Full Changelog](https://github.com/folio-org/mod-quick-marc/compare/v2.0.0...v2.0.1)

### Bug Fixes
* [MODQM-85](https://issues.folio.org/browse/MODQM-85) - Could not Derive a new MARC bib record

## 2.0.0 - Released

The primary focus of this release was to support duplication of MARC records and migration of the module to Spring framework

[Full Changelog](https://github.com/folio-org/mod-quick-marc/compare/v1.2.2...v2.0.0)

### Stories

 * [MODQM-52](https://issues.folio.org/browse/MODQM-52) - Update MARC 005 field and send to SRS along with other quickMARC changes
 * [MODQM-56](https://issues.folio.org/browse/MODQM-56) - Add personal data disclosure form
 * [MODQM-63](https://issues.folio.org/browse/MODQM-63) - Migrate to Spring stack
 * [MODQM-78](https://issues.folio.org/browse/MODQM-78) - Implement GET status endpoint
 * [MODQM-79](https://issues.folio.org/browse/MODQM-79) - Listen DI-events to update creation status
 * [MODQM-80](https://issues.folio.org/browse/MODQM-80) - Integration between "mod-quick-marc" and srm

## 1.3.0 - Unreleased

## 1.2.2 - Released

The only focus of this patch release was to update schemas reference

[Full Changelog](https://github.com/folio-org/mod-quick-marc/compare/v1.2.1...v1.2.2)

### Bug Fixes
 * [MODQM-47](https://issues.folio.org/browse/MODQM-47) - Bug with subfield and spaces when editing in quickMARC
 * [MODQM-46](https://issues.folio.org/browse/MODQM-46) - Adjust the quickMARC edit UI to show a fill character for blank positions in 00X fields and indicators

## 1.2.1 - Released

The only focus of this patch release was to update schemas reference

[Full Changelog](https://github.com/folio-org/mod-quick-marc/compare/v1.2.0...v1.2.1)

### Bug Fixes
 * [MODQM-48](https://issues.folio.org/browse/MODQM-48) - Some Source=MARC records are not accessible via quickMARC

## 1.2.0 - Released

The only focus of this release was to migrate on JDK11 and to update RMB

[Full Changelog](https://github.com/folio-org/mod-quick-marc/compare/v1.1.1...v1.2.0)

### Stories
 * [MODQM-31](https://issues.folio.org/browse/MODQM-31) - mod-quick-marc: RMB Update
 * [MODQM-26](https://issues.folio.org/browse/MODQM-26) - Migrate mod-quick-marc to JDK 11

## 1.1.1 - Released

The only focus of this patch release was to improve fixed-length CFs 006/007 processing

[Full Changelog](https://github.com/folio-org/mod-quick-marc/compare/v1.1.0...v1.1.1)

### Bug Fixes
 * [MODQM-35](https://issues.folio.org/browse/MODQM-35) - GET /records-editor/records returns 422 when record has 006 field

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
