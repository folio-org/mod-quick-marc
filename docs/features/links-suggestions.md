---
feature_id: links-suggestions
title: Links Suggestions
updated: 2026-04-01
---

# Links Suggestions

## What it does
Accepts a Bibliographic MARC record and returns the same record enriched with suggested authority links on eligible fields. The quickMARC record is converted to the internal SRS representation, forwarded to the entity-links suggestions API, and the first record in the response is converted back to `quickMarcView` format. If the suggestions API returns no records, the original submitted record is returned unchanged. The `ignoreAutoLinkingEnabled` flag is forwarded directly to the upstream API and is not evaluated locally.

## Why it exists
Manually linking MARC bibliographic fields to authority records is time-consuming and error-prone. This endpoint enables the quickMARC editor to offer one-click auto-linking by fetching machine-generated authority link candidates for the fields the cataloguer is editing.

## Entry point(s)
| Method | Path | Description |
|--------|------|-------------|
| POST | /records-editor/links/suggestion | Returns a `quickMarcView` with authority link suggestions applied |

## Business rules and constraints

#### Request parameters
- **Authority search parameter:** The `authoritySearchParameter` query parameter controls which authority field is used to match candidates: `ID` (authority UUID) or `NATURAL_ID` (default). When not provided, `NATURAL_ID` is used.
- **Auto-linking flag:** The `ignoreAutoLinkingEnabled` flag, when `true`, includes fields regardless of whether their linking rule has auto-linking enabled. Default is `false`.

#### Processing
- **Format conversion:** The request record is converted to the internal SRS representation and wrapped in a single-record payload before being sent to the entity-links suggestions API (`POST /links-suggestions/marc`). The first record in the response is converted back to `quickMarcView` format.
- **Linking rules:** Field-to-authority matching and link detail population are governed by linking rules fetched from the entity-links service. Only fields that match a configured rule receive link details; this filtering is performed by the upstream service, not locally.

#### Response behavior
- **No suggestions fallback:** If the suggestions response contains no records, the original submitted `quickMarcView` is returned without modification.

## Error behavior
- **400 Bad Request** – malformed or unreadable request body, or missing required parameters.
- **422 Unprocessable Content** – MARC field validation failed.
- **upstream errors** – HTTP errors from the entity-links suggestions API are propagated back to the caller with the same status code.
- **500 Internal Server Error** – unexpected server-side failure.

## Caching
Linking rules are cached in the `linking-rules-results` Caffeine cache keyed by tenant ID. The cache uses a **1-hour access TTL** (`expireAfterAccess=3600s`) with a maximum of 500 entries. Rules are fetched from the entity-links service on first access per tenant and reused across suggestion requests.

## Configuration (if applicable)
| Variable | Purpose |
|----------|---------|
| `spring.cache.caffeine.spec` | Caffeine spec for all caches including `linking-rules-results` (default: `maximumSize=500,expireAfterAccess=3600s`) |

## Dependencies and interactions
- **mod-entities-links** (links suggestions API at `POST /links-suggestions/marc`) – receives the single-record SRS payload and returns it with link suggestions applied.
- **mod-entities-links** (linking rules API at `GET /linking-rules/instance-authority`) – provides the set of rules used to match bibliographic fields to authority records; results are served from the `linking-rules-results` cache.
