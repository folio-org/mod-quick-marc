---
feature_id: links-suggestions
title: Links Suggestions
updated: 2026-04-01
---

# Links Suggestions

## What it does
Accepts a Bibliographic MARC record and returns the same record enriched with suggested authority links on eligible fields. The service delegates to the entity-links suggestions API, which analyses the MARC fields against the authority file and returns linking candidates. If no suggestions are returned, the original record is returned unchanged.

## Why it exists
Manually linking MARC bibliographic fields to authority records is time-consuming and error-prone. This endpoint enables the quickMARC editor to offer one-click auto-linking by fetching machine-generated authority link candidates for the fields the cataloguer is editing.

## Entry point(s)
| Method | Path | Description |
|--------|------|-------------|
| POST | /records-editor/links/suggestion | Returns a `quickMarcView` with authority link suggestions applied |

## Business rules and constraints
- The `authoritySearchParameter` query parameter controls which authority field is used to match candidates: `ID` (authority UUID) or `NATURAL_ID` (default). When not provided, `NATURAL_ID` is used.
- The `ignoreAutoLinkingEnabled` flag, when `true`, includes fields regardless of whether their linking rule has auto-linking enabled. Default is `false`.
- The request record is converted to the internal SRS representation before being sent to the entity-links suggestions API. The first record in the response is converted back to `quickMarcView` format.
- If the suggestions response contains no records, the original submitted `quickMarcView` is returned without modification.
- Only fields that match a configured linking rule (from the linking-rules service) receive link details.

## Caching
Linking rules are cached in the `linking-rules-results` Caffeine cache (maximum 500 entries, 1-hour access TTL). The rules are fetched from the entity-links service and reused across suggestion requests.

## Dependencies and interactions
- **mod-entities-links** (links suggestions API at `/marc`) – the primary upstream service; receives the MARC record and returns it with link suggestions applied.
- **mod-entities-links** (linking rules API at `/instance-authority`) – provides the set of rules used to match bibliographic fields to authority records and to populate link details in the response.
