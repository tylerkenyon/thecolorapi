# thecolorapi-java (Java 21 Maven Library)

`thecolorapi-java` is a Java helper library that mirrors the existing The Color API HTTP surface 1:1 and is designed for Java 21 projects using Maven.

It provides helper methods for all existing API routes:

- `/id`
- `/scheme`
- `/colorbox`
- `/schemebox`
- `/random`

## Maven setup

From `/home/runner/work/thecolorapi/thecolorapi/java`:

```bash
mvn clean test
mvn clean package
```

To install locally:

```bash
mvn clean install
```

## Java version

This module targets Java 21.

```xml
<maven.compiler.source>21</maven.compiler.source>
<maven.compiler.target>21</maven.compiler.target>
```

## API helper usage

```java
import com.thecolorapi.client.ColorApiClient;
import com.thecolorapi.client.ColorApiResponse;
import com.thecolorapi.client.SchemeMode;

ColorApiClient client = new ColorApiClient();

// /id helpers
ColorApiResponse byHex = client.identifyByHex("0047AB");
ColorApiResponse byRgb = client.identifyByRgb(0, 71, 171);
ColorApiResponse byHsl = client.identifyByHsl(215, 100, 34);
ColorApiResponse byHsv = client.identifyByHsv(215, 100, 67);
ColorApiResponse byCmyk = client.identifyByCmyk(100, 58, 0, 33);

// /scheme helper
ColorApiResponse scheme = client.schemeByHex("0047AB", SchemeMode.TRIAD, 6);

// /colorbox URL helper
var colorBox = client.colorBoxUri(
    com.thecolorapi.client.ColorQuery.hex("0047AB"),
    350,
    350,
    true
);

// /schemebox URL helper
var schemeBox = client.schemeBoxUri(
    com.thecolorapi.client.ColorQuery.hex("0047AB"),
    SchemeMode.MONOCHROME,
    5,
    350,
    350,
    true
);

// /random URL helper
var random = client.randomColorUri();
```

## Helper classes

- `ColorApiClient`: main client and route-specific helpers
- `ColorApiResponse`: immutable record containing `statusCode`, `body`, and `uri`
- `ColorQuery`: input builder for `hex`, `rgb`, `hsl`, `hsv`, `cmyk`
- `SchemeMode`: enum matching API modes (`monochrome`, `triad`, etc.)

## 1:1 route and query mapping

### `/id`

- `identifyByHex(hex)` -> `/id?hex=...`
- `identifyByRgb(r,g,b)` -> `/id?rgb=r,g,b`
- `identifyByHsl(h,s,l)` -> `/id?hsl=h,s,l`
- `identifyByHsv(h,s,v)` -> `/id?hsv=h,s,v`
- `identifyByCmyk(c,m,y,k)` -> `/id?cmyk=c,m,y,k`

### `/scheme`

- `schemeByHex(hex, mode, count)` -> `/scheme?hex=...&mode=...&count=...`
- `scheme(colorQuery, mode, count)` supports all `ColorQuery` inputs and optional `format`

### `/colorbox`

- `colorBoxUri(colorQuery, width, height, named)` -> `/colorbox?...`

### `/schemebox`

- `schemeBoxUri(colorQuery, mode, count, width, height, named)` -> `/schemebox?...`

### `/random`

- `randomColorUri()` -> `/random`

## Notes

- URL query values are UTF-8 encoded.
- Dimension and count helpers validate that values are greater than zero.
- The `format` parameter can be passed to `identify(..., format)` and `scheme(..., format)` to mirror API behavior (`json`, `html`, `svg`).
