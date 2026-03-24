# thecolorapi-java (Java 21 Local Library)

This module is now a **fully local Java 21 library**.

It does not call `https://www.thecolorapi.com` for color computation. Instead, it ports the core JavaScript logic from:

- `/home/runner/work/thecolorapi/thecolorapi/lib/colored.js`
- `/home/runner/work/thecolorapi/thecolorapi/lib/schemer.js`
- `/home/runner/work/thecolorapi/thecolorapi/lib/cutils.js`

## What is local now

- Color identification payload generation (`/id` equivalent)
  - hex/rgb/hsl/hsv/cmyk input support
  - rgb/hsl/hsv/cmyk/XYZ output sections
  - nearest color-name lookup using local `colorNames.json`
  - contrast recommendation (`#000000` or `#FFFFFF`)
- Scheme generation (`/scheme` equivalent)
  - `monochrome`, `monochrome-dark`, `monochrome-light`, `analogic`, `complement`, `analogic-complement`, `triad`, `quad`
  - plus `advanced` mode for richer palette behavior
- SVG rendering helpers
  - `colorBoxSvg` (`/colorbox` equivalent)
  - `schemeBoxSvg` (`/schemebox` equivalent)
- Random color generation (`/random` equivalent as local `randomHex()`)

## Maven usage

From `/home/runner/work/thecolorapi/thecolorapi/java`:

```bash
mvn clean test
mvn clean package
```

## Java version

Targets Java 21.

## API usage

```java
import com.thecolorapi.client.ColorApiClient;
import com.thecolorapi.client.ColorQuery;
import com.thecolorapi.client.SchemeMode;

ColorApiClient client = new ColorApiClient();

// /id equivalent
var color = client.identifyByHex("0047AB");
var byRgb = client.identifyByRgb(0, 71, 171);

// /scheme equivalent
var triad = client.schemeByHex("0047AB", SchemeMode.TRIAD, 6);

// advanced scheme mode
var advanced = client.advancedSchemeByHex("0047AB", 8);

// /colorbox and /schemebox equivalents (SVG strings)
String colorSvg = client.colorBoxSvg(ColorQuery.hex("0047AB"), 120, 80, true);
String schemeSvg = client.schemeBoxSvg(ColorQuery.hex("0047AB"), SchemeMode.MONOCHROME, 5, 120, 220, false);

// /random equivalent
String randomHex = client.randomHex();
```

## Notes

- `ColorEngine` contains the local ported logic and data shaping.
- `colorNames.json` is loaded from `java/src/main/resources/colorNames.json`.
- For strict parity, output map keys match the existing API-style payload shape (`hex`, `rgb`, `hsl`, `hsv`, `cmyk`, `XYZ`, `name`, `contrast`, `image`, `_links`, `_embedded`).
