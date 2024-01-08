# mod-quick-marc

Copyright (C) 2020-2023 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file [LICENSE](LICENSE) for more information.

## Introduction
Spring-based module that provides API for quickMARC - in-app editor for MARC records in SRS.

## Additional information
quickMARC API provides the following URLs:

| Method | URL                                                                                                      | Permissions                               | Description                                                          | 
|--------|----------------------------------------------------------------------------------------------------------|-------------------------------------------|----------------------------------------------------------------------|
| GET    | /records-editor/records?externalId={externalId}                                                          | marc-records-editor.item.get              | Retrieves MARC record by external id                                 |
| POST   | /records-editor/records                                                                                  | marc-records-editor.item.post             | Create a new MARC and mapped records (instance, holdings, authority) |
| PUT    | /records-editor/records/{recordId}                                                                       | marc-records-editor.item.get              | Updates MARC and mapped records (instance, holdings, authority)      |
| GET    | /records-editor/records/status?qmRecordId={qmRecordId}                                                   | marc-records-editor.status.item.get       | Retrieves status of records creation                                 |
| POST   | /records-editor/links/suggestion?authoritySearchParameter={ID/NATURAL_ID}&ignoreAutoLinkingEnabled=false | marc-records-editor.links.suggestion.post | Suggest links for record collection                                  |
| GET    | /marc-specifications/{recordType}/{fieldTag}                                                             | marc-specifications.item.get              | Retrieves MARC specification by record type and MARC field tag       |

More detail can be found on quickMARC wiki-page: [WIKI quickMARC](https://wiki.folio.org/pages/viewpage.action?pageId=36571766).

### Validation rules in quickMARC
#### MARC Holdings Validation rules

| MARC field | Validation rule                    |
|:-----------|:-----------------------------------|
| **008**    | Required field  <br/> Unique field |    
| **852**    | Required field  <br/> Unique field |    

#### MARC Authority Validation rules

| MARC field | Validation rule                    |
|:-----------|:-----------------------------------|
| **1xx**    | Required field  <br/> Unique field |    
| **010**    | Optional field  <br/> Unique field |    

#### MARC Bibliographic Validation rules

| MARC field | Validation rule                    |
|:-----------|:-----------------------------------|
| **245**    | Required field  <br/> Unique field |    

### Required Permissions
Institutional users should be granted the following permissions in order to use this quickMARC API:
- `records-editor.all`

### Issue tracker
See project [MODQM](https://issues.folio.org/browse/MODQM)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).


### Other documentation
Other [modules](https://dev.folio.org/source-code/#server-side) are described,
with further FOLIO Developer documentation at
[dev.folio.org](https://dev.folio.org/)
