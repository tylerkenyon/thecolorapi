# Java Library Overview

## Purpose

`thecolorapi-java` is now a local-first Java 21 library that executes color identification, conversion, naming, scheme generation, and SVG rendering in-process.

No outbound API call is required for core functionality.

## Package layout

- `com.thecolorapi.client.ColorApiClient` — public entry point and convenience methods.
- `com.thecolorapi.client.ColorQuery` — immutable typed and parser-based color input model.
- `com.thecolorapi.client.SchemeMode` — supported scheme modes.
- `com.thecolorapi.client.ColorEngine` — internal algorithm implementation and payload shaping.
- `com.thecolorapi.client.ColorApiResponse` — compatibility record retained from wrapper era.

## Local equivalents of legacy routes

- `/id` -> `identify(...)` / `identifyBy*` / `identifyFromQuery(...)` / `identifyUnknown(...)`
- `/scheme` -> `scheme(...)` / `schemeByHex(...)` / `advancedSchemeByHex(...)`
- `/colorbox` -> `colorBoxSvg(...)`
- `/schemebox` -> `schemeBoxSvg(...)`
- `/random` -> `randomHex()`

## Data model shape

Color payload map keys:

- `hex`
- `rgb`
- `hsl`
- `hsv`
- `cmyk`
- `XYZ`
- `name`
- `contrast`
- `image`
- `_links`
- `_embedded`

Scheme payload map keys:

- `mode`
- `count`
- `colors`
- `seed`
- `image`
- `_links`
- `_embedded`

## Compatibility and parity goals

The Java implementation mirrors the behavior of the legacy JS implementation in `lib/colored.js`, `lib/schemer.js`, and `lib/cutils.js` for:

- query-style parsing and precedence
- channel conversion paths
- nearest named-color resolution
- scheme weighting and generation
- output structure conventions

## Build and test

From repository root:

```bash
cd java
mvn clean test
mvn clean package
```
