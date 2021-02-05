# mod-quick-marc

Copyright (C) 2020 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file [LICENSE](LICENSE) for more information.

## Introduction
API for quickMARC - in-app editor for MARC records in SRS.

## Additional information
quickMARC API provides the following URLs:

|  Method | URL| Permissions  | Description  | 
|---|---|---|---|
| GET | /records-editor/records?instanceId={instanceId}  |records-editor.records.item.get   | Retrieves QuickMarc by instance's id  |
| PUT | /records-editor/records/{recordId}  |records-editor.records.item.get   | Updates SRS record |
| GET | /records-editor/records/status?qmRecordId={qmRecordId}  |records-editor.records.status.item.get   | Retrieves status of MARC bibliographic record creation  |

More detail can be found on quickMARC wiki-page: [WIKI quickMARC](https://wiki.folio.org/pages/viewpage.action?pageId=36571766).

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
