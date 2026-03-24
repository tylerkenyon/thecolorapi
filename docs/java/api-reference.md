# Java API Reference

## `ColorApiClient`

### `Map<String, Object> identify(ColorQuery colorQuery)`
Resolves a full color payload from a typed query.

### `Map<String, Object> identifyByHex(String hex)`
Hex convenience wrapper.

### `Map<String, Object> identifyByRgb(int r, int g, int b)`
RGB convenience wrapper.

### `Map<String, Object> identifyByHsl(int h, int s, int l)`
HSL convenience wrapper.

### `Map<String, Object> identifyByHsv(int h, int s, int v)`
HSV convenience wrapper.

### `Map<String, Object> identifyByCmyk(int c, int m, int y, int k)`
CMYK convenience wrapper.

### `Map<String, Object> identifyFromQuery(String hex, String rgb, String hsl, String cmyk, String hsv)`
Parses legacy query-style values and applies legacy precedence:

1. `rgb`
2. `hex`
3. `hsl`
4. `hsv`
5. `cmyk`

### `Map<String, Object> identifyUnknown(String input)`
Auto-detects input style from unknown token style:

- `rgb(...)`
- `hsl(...)`
- `hsv(...)`
- `cmyk(...)`
- hex-like raw value

### `Map<String, Object> scheme(ColorQuery colorQuery, SchemeMode mode, int count)`
Generates a scheme payload from a seed query.

### `Map<String, Object> schemeByHex(String hex, SchemeMode mode, int count)`
Hex scheme convenience wrapper.

### `Map<String, Object> advancedSchemeByHex(String hex, int count)`
Shortcut for `SchemeMode.ADVANCED`.

### `String randomHex()`
Returns random uppercase `#RRGGBB`.

### `String colorBoxSvg(ColorQuery colorQuery, Integer width, Integer height, Boolean named)`
Generates single-color SVG swatch.

- width default: `100`
- height default: `100`
- `named=false` suppresses text label

### `String schemeBoxSvg(ColorQuery colorQuery, SchemeMode mode, Integer count, Integer width, Integer height, Boolean named)`
Generates scheme SVG swatch stack.

- count default: `5`
- width default: `100`
- height default: `200`
- `named=false` suppresses labels

---

## `ColorQuery`

### Constructors

- `hex(String)`
- `rgb(int,int,int)`
- `hsl(int,int,int)`
- `hsv(int,int,int)`
- `cmyk(int,int,int,int)`
- `hslFraction(double,double,double)`

### Parsers

- `parseQueryColors(String hex, String rgb, String hsl, String cmyk, String hsv)`
- `parseUnknownType(String input)`

`parseQueryColors(...)` supports both raw CSV channels and functional wrappers like `rgb(1,2,3)`.
Decimal presence is treated as fractional input semantics where applicable.

---

## `SchemeMode`

- `MONOCHROME`
- `MONOCHROME_DARK`
- `MONOCHROME_LIGHT`
- `ANALOGIC`
- `COMPLEMENT`
- `ANALOGIC_COMPLEMENT`
- `TRIAD`
- `QUAD`
- `ADVANCED`

Each mode exposes route-compatible lowercase token via `value()`.

---

## `ColorApiResponse`

Compatibility record from previous wrapper client era:

`record ColorApiResponse(int statusCode, String body, URI uri)`

Retained for migration safety; local-first workflows typically use map payloads directly.
