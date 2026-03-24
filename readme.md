# The Color API — Java Local Library

A local-first Java 21 color library that implements The Color API behavior directly in-process.

## About this library

- No runtime dependency on outbound color API calls.
- 1:1 parity-oriented payload structure and mode behavior.
- Rich, deterministic scheme generation including an enhanced `advanced` mode.
- SVG generation helpers for color and scheme swatches.

## Core capabilities

- Identify/normalize color from: hex, rgb, hsl, hsv, cmyk
- Convert and return: rgb, hsl, hsv, cmyk, XYZ + fractions
- Nearest named-color resolution from local dataset
- Contrast recommendation (`#000000` or `#FFFFFF`)
- Scheme generation modes:
  - monochrome
  - monochrome-dark
  - monochrome-light
  - analogic
  - complement
  - analogic-complement
  - triad
  - quad
  - advanced
- Local SVG rendering:
  - color box
  - scheme box
- Random hex generation

## Quick start

```bash
cd java
mvn clean test
mvn clean package
```

```java
import com.thecolorapi.client.ColorApiClient;
import com.thecolorapi.client.ColorQuery;
import com.thecolorapi.client.SchemeMode;

ColorApiClient client = new ColorApiClient();

var color = client.identifyByHex("0047AB");
var scheme = client.schemeByHex("0047AB", SchemeMode.TRIAD, 6);
var advanced = client.advancedSchemeByHex("0047AB", 8);

String colorSvg = client.colorBoxSvg(ColorQuery.hex("0047AB"), 120, 80, true);
String schemeSvg = client.schemeBoxSvg(ColorQuery.hex("0047AB"), SchemeMode.MONOCHROME, 5, 120, 220, false);
```

## Documentation

Detailed Java docs:

- `docs/java/overview.md`
- `docs/java/api-reference.md`
- `docs/java/payload-spec.md`
- `docs/java/migration-and-parity.md`

## Notes

- Public API includes Javadocs for IDE tooltips (IntelliJ/Eclipse).
- Legacy `ColorApiResponse` record remains for compatibility with earlier wrapper usage.
