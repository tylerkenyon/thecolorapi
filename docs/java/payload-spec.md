# Payload Specification (Local Java)

## Color payload

The color payload is a nested map with keys matching the legacy API layout.

## Top-level keys

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

## Field details

### `hex`
- `value` (e.g. `#0047AB`)
- `clean` (e.g. `0047AB`)

### `rgb`
- `r`, `g`, `b` (integer channels)
- `fraction` with `r`, `g`, `b` in 0..1
- `value` formatted string

### `hsl`
- `h`, `s`, `l` rounded integers
- `fraction` with `h`, `s`, `l` in 0..1
- `value` formatted string

### `hsv`
- `h`, `s`, `v` rounded integers
- `fraction` with `h`, `s`, `v` in 0..1
- `value` formatted string

### `cmyk`
- `c`, `m`, `y`, `k` rounded integers
- `fraction` with `c`, `m`, `y`, `k` in 0..1
- `value` formatted string

### `XYZ`
- `X`, `Y`, `Z` rounded integers
- `fraction` with `X`, `Y`, `Z`
- `value` formatted string

### `name`
- `value` nearest or exact color name
- `closest_named_hex`
- `exact_match_name`
- `distance`

### `contrast`
- `value` either `#000000` or `#FFFFFF` based on WCAG-style contrast ratio comparison.

### `image`
- `bare`
- `named`

These are compatibility-style strings preserved in payload output shape.

### `_links`
- `self.href` route-style path to `/id` equivalent

### `_embedded`
- reserved empty map

---

## Scheme payload

## Top-level keys

- `mode`
- `count`
- `colors`
- `seed`
- `image`
- `_links`
- `_embedded`

### `colors`
List of color payload maps.

### `seed`
Full color payload map for the input seed.

### `_links.schemes`
Map from scheme token to route-style compatibility path.

Includes all base modes and `advanced`.

---

## SVG behavior

### colorBox
- Draws full-size rectangle in seed color.
- Optional centered text using contrast color.

### schemeBox
- Divides canvas into equal-height sections.
- Draws one rectangle per generated scheme color.
- Optional centered label text per section.
