package com.thecolorapi.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ColorEngine {
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final List<NamedColor> NAMED_COLORS = loadNamedColors();

  private ColorEngine() {
  }

  static Map<String, Object> colorMe(ColorQuery query) {
    Objects.requireNonNull(query, "query is required");

    return switch (query.type()) {
      case HEX -> fromHex(query.hex());
      case RGB -> {
        Map<String, Double> rgb = query.values();
        int r = rgb.get("r").intValue();
        int g = rgb.get("g").intValue();
        int b = rgb.get("b").intValue();
        yield fromHex(rgbToHex(r, g, b));
      }
      case HSL -> {
        Map<String, Double> hsl = query.values();
        double h = query.fraction() ? hsl.get("h") : hsl.get("h") / 360d;
        double s = query.fraction() ? hsl.get("s") : hsl.get("s") / 100d;
        double l = query.fraction() ? hsl.get("l") : hsl.get("l") / 100d;
        double[] rgb = hslToRgb(h, s, l);
        yield fromHex(rgbFracToHex(rgb[0], rgb[1], rgb[2]));
      }
      case HSV -> {
        Map<String, Double> hsv = query.values();
        double h = query.fraction() ? hsv.get("h") : hsv.get("h") / 360d;
        double s = query.fraction() ? hsv.get("s") : hsv.get("s") / 100d;
        double v = query.fraction() ? hsv.get("v") : hsv.get("v") / 100d;
        double[] rgb = hsvToRgb(h, s, v);
        yield fromHex(rgbFracToHex(rgb[0], rgb[1], rgb[2]));
      }
      case CMYK -> {
        Map<String, Double> cmyk = query.values();
        double c = query.fraction() ? cmyk.get("c") : cmyk.get("c") / 100d;
        double m = query.fraction() ? cmyk.get("m") : cmyk.get("m") / 100d;
        double y = query.fraction() ? cmyk.get("y") : cmyk.get("y") / 100d;
        double k = query.fraction() ? cmyk.get("k") : cmyk.get("k") / 100d;
        double[] rgb = cmykToRgb(c, m, y, k);
        yield fromHex(rgbFracToHex(rgb[0], rgb[1], rgb[2]));
      }
    };
  }

  static Map<String, Object> scheme(SchemeMode mode, int count, Map<String, Object> seed) {
    if (count <= 0) {
      throw new IllegalArgumentException("count must be greater than 0");
    }
    Objects.requireNonNull(mode, "mode is required");
    Objects.requireNonNull(seed, "seed is required");

    List<Map<String, Object>> colors = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      colors.add(seed);
    }
    List<Map<String, Object>> generated = generateScheme(mode.value(), colors, seed);

    Map<String, Object> hex = map(seed.get("hex"));
    String clean = str(hex.get("clean"));

    Map<String, Object> links = new LinkedHashMap<>();
    links.put("self", "/scheme?hex=" + clean + "&mode=" + mode.value() + "&count=" + count);
    Map<String, String> schemes = new LinkedHashMap<>();
    for (SchemeMode m : SchemeMode.values()) {
      if (m == SchemeMode.ADVANCED) {
        continue;
      }
      schemes.put(m.value(), "/scheme?hex=" + clean + "&mode=" + m.value() + "&count=" + count);
    }
    schemes.put("advanced", "/scheme?hex=" + clean + "&mode=advanced&count=" + count);
    links.put("schemes", schemes);

    Map<String, Object> image = new LinkedHashMap<>();
    image.put("bare", "https://www.thecolorapi.com/scheme?format=svg&named=false&hex=" + clean + "&mode=" + mode.value() + "&count=" + count);
    image.put("named", "https://www.thecolorapi.com/scheme?format=svg&hex=" + clean + "&mode=" + mode.value() + "&count=" + count);

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("mode", mode.value());
    response.put("count", count);
    response.put("colors", generated);
    response.put("seed", seed);
    response.put("image", image);
    response.put("_links", links);
    response.put("_embedded", Map.of());
    return response;
  }

  static String colorBoxSvg(Map<String, Object> color, Integer width, Integer height, Boolean named) {
    int w = width == null ? 100 : width;
    int h = height == null ? 100 : height;
    if (w <= 0 || h <= 0) {
      throw new IllegalArgumentException("width and height must be greater than 0");
    }

    String hex = str(map(color.get("hex")).get("value"));
    String name = str(map(color.get("name")).get("value"));
    String contrast = str(map(color.get("contrast")).get("value"));

    String text = Boolean.FALSE.equals(named) ? "" : escapeXml(name);
    return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + w + "\" height=\"" + h + "\">"
        + "<rect width=\"100%\" height=\"100%\" fill=\"" + hex + "\"/>"
        + (text.isEmpty() ? "" : "<text x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" fill=\"" + contrast + "\" font-size=\"14\">" + text + "</text>")
        + "</svg>";
  }

  static String schemeBoxSvg(Map<String, Object> scheme, Integer width, Integer height, Boolean named) {
    int w = width == null ? 100 : width;
    int h = height == null ? 200 : height;
    if (w <= 0 || h <= 0) {
      throw new IllegalArgumentException("width and height must be greater than 0");
    }

    List<Map<String, Object>> colors = listOfMaps(scheme.get("colors"));
    int section = Math.max(1, Math.round((float) h / Math.max(1, colors.size())));
    StringBuilder sb = new StringBuilder();
    sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(w).append("\" height=\"").append(h).append("\">");

    int y = 0;
    for (Map<String, Object> color : colors) {
      String hex = str(map(color.get("hex")).get("value"));
      String name = str(map(color.get("name")).get("value"));
      String contrast = str(map(color.get("contrast")).get("value"));
      sb.append("<rect x=\"0\" y=\"").append(y).append("\" width=\"").append(w).append("\" height=\"").append(section).append("\" fill=\"").append(hex).append("\"/>");
      if (!Boolean.FALSE.equals(named)) {
        sb.append("<text x=\"50%\" y=\"").append(y + (section / 2)).append("\" dominant-baseline=\"middle\" text-anchor=\"middle\" fill=\"").append(contrast).append("\" font-size=\"12\">")
            .append(escapeXml(name)).append("</text>");
      }
      y += section;
    }
    sb.append("</svg>");
    return sb.toString();
  }

  static String randomHex() {
    String letters = "0123456789ABCDEF";
    StringBuilder color = new StringBuilder("#");
    for (int i = 0; i < 6; i++) {
      color.append(letters.charAt(RANDOM.nextInt(16)));
    }
    return color.toString();
  }

  private static Map<String, Object> fromHex(String hexInput) {
    String hex = hexCheck(hexInput);
    int[] rgb = hexToRgb(hex, false);
    NameResult nearest = nearestNamedHex(hex);

    Map<String, Object> color = new LinkedHashMap<>();

    Map<String, Object> hexObj = new LinkedHashMap<>();
    hexObj.put("value", hex);
    hexObj.put("clean", hex.substring(1));
    color.put("hex", hexObj);

    Map<String, Object> rgbObj = new LinkedHashMap<>();
    rgbObj.put("r", rgb[0]);
    rgbObj.put("g", rgb[1]);
    rgbObj.put("b", rgb[2]);
    Map<String, Object> rgbFrac = new LinkedHashMap<>();
    rgbFrac.put("r", rgb[0] / 255d);
    rgbFrac.put("g", rgb[1] / 255d);
    rgbFrac.put("b", rgb[2] / 255d);
    rgbObj.put("fraction", rgbFrac);
    rgbObj.put("value", "rgb(" + rgb[0] + ", " + rgb[1] + ", " + rgb[2] + ")");
    color.put("rgb", rgbObj);

    double[] hsl = rgbToHsl(rgbFrac);
    Map<String, Object> hslObj = new LinkedHashMap<>();
    hslObj.put("h", (int) Math.round(hsl[0] * 360));
    hslObj.put("s", (int) Math.round(hsl[1] * 100));
    hslObj.put("l", (int) Math.round(hsl[2] * 100));
    Map<String, Object> hslFrac = new LinkedHashMap<>();
    hslFrac.put("h", hsl[0]);
    hslFrac.put("s", hsl[1]);
    hslFrac.put("l", hsl[2]);
    hslObj.put("fraction", hslFrac);
    hslObj.put("value", "hsl(" + Math.round(hsl[0] * 360) + ", " + Math.round(hsl[1] * 100) + "%, " + Math.round(hsl[2] * 100) + "%)");
    color.put("hsl", hslObj);

    double[] hsv = rgbToHsv(rgbFrac);
    Map<String, Object> hsvObj = new LinkedHashMap<>();
    hsvObj.put("h", (int) Math.round(hsv[0] * 360));
    hsvObj.put("s", (int) Math.round(hsv[1] * 100));
    hsvObj.put("v", (int) Math.round(hsv[2] * 100));
    Map<String, Object> hsvFrac = new LinkedHashMap<>();
    hsvFrac.put("h", hsv[0]);
    hsvFrac.put("s", hsv[1]);
    hsvFrac.put("v", hsv[2]);
    hsvObj.put("fraction", hsvFrac);
    hsvObj.put("value", "hsv(" + Math.round(hsv[0] * 360) + ", " + Math.round(hsv[1] * 100) + "%, " + Math.round(hsv[2] * 100) + "%)");
    color.put("hsv", hsvObj);

    double[] xyz = rgbToXyz(rgbFrac);
    Map<String, Object> xyzObj = new LinkedHashMap<>();
    xyzObj.put("X", (int) Math.round(xyz[0] * 100));
    xyzObj.put("Y", (int) Math.round(xyz[1] * 100));
    xyzObj.put("Z", (int) Math.round(xyz[2] * 100));
    Map<String, Object> xyzFrac = new LinkedHashMap<>();
    xyzFrac.put("X", xyz[0]);
    xyzFrac.put("Y", xyz[1]);
    xyzFrac.put("Z", xyz[2]);
    xyzObj.put("fraction", xyzFrac);
    xyzObj.put("value", "XYZ(" + Math.round(xyz[0] * 100) + ", " + Math.round(xyz[1] * 100) + ", " + Math.round(xyz[2] * 100) + ")");
    color.put("XYZ", xyzObj);

    double[] cmyk = rgbToCmyk(rgbFrac);
    Map<String, Object> cmykObj = new LinkedHashMap<>();
    cmykObj.put("c", (int) Math.round(cmyk[0] * 100));
    cmykObj.put("m", (int) Math.round(cmyk[1] * 100));
    cmykObj.put("y", (int) Math.round(cmyk[2] * 100));
    cmykObj.put("k", (int) Math.round(cmyk[3] * 100));
    Map<String, Object> cmykFrac = new LinkedHashMap<>();
    cmykFrac.put("c", cmyk[0]);
    cmykFrac.put("m", cmyk[1]);
    cmykFrac.put("y", cmyk[2]);
    cmykFrac.put("k", cmyk[3]);
    cmykObj.put("fraction", cmykFrac);
    cmykObj.put("value", "cmyk(" + Math.round(cmyk[0] * 100) + ", " + Math.round(cmyk[1] * 100) + ", " + Math.round(cmyk[2] * 100) + ", " + Math.round(cmyk[3] * 100) + ")");
    color.put("cmyk", cmykObj);

    Map<String, Object> nameObj = new LinkedHashMap<>();
    nameObj.put("value", nearest.name);
    nameObj.put("closest_named_hex", nearest.hex);
    nameObj.put("exact_match_name", nearest.exact);
    nameObj.put("distance", nearest.distance);
    color.put("name", nameObj);

    int[] contrastRgb = highestContrast(new int[]{0, 0, 0}, new int[]{255, 255, 255}, rgb);
    color.put("contrast", Map.of("value", rgbToHex(contrastRgb[0], contrastRgb[1], contrastRgb[2])));

    String clean = hex.substring(1);
    Map<String, Object> image = new LinkedHashMap<>();
    image.put("bare", "https://www.thecolorapi.com/id?format=svg&named=false&hex=" + clean);
    image.put("named", "https://www.thecolorapi.com/id?format=svg&hex=" + clean);
    color.put("image", image);

    color.put("_links", Map.of("self", Map.of("href", "/id?hex=" + clean)));
    color.put("_embedded", Map.of());
    return color;
  }

  private static List<Map<String, Object>> generateScheme(String mode, List<Map<String, Object>> colors, Map<String, Object> color) {
    List<Group> scheme = schemeDefinition(mode);

    List<Double> ratios = new ArrayList<>();
    List<Integer> counts = new ArrayList<>();
    for (Group group : scheme) {
      ratios.add(group.ratio());
      counts.add(0);
    }

    double colorWeight = 1d / colors.size();
    for (int i = 0; i < colors.size(); i++) {
      double maxRatio = -1;
      int maxIdx = -1;
      for (int j = 0; j < ratios.size(); j++) {
        if (ratios.get(j) > maxRatio) {
          maxRatio = ratios.get(j);
          maxIdx = j;
        }
      }
      ratios.set(maxIdx, ratios.get(maxIdx) - colorWeight);
      counts.set(maxIdx, counts.get(maxIdx) + 1);
    }

    Map<String, Object> hslObj = map(color.get("hsl"));
    Map<String, Object> frac = map(hslObj.get("fraction"));
    double baseH = num(frac.get("h"));
    double baseS = num(frac.get("s"));
    double baseL = num(frac.get("l"));

    double seed = 0.5;
    double seedStep = seed / colors.size();
    double hSeed = 0;
    double sMaxSeed = Math.min(1, baseS + seed / 2);
    double sMinSeed = Math.max(0, sMaxSeed - seed);
    double sSeed = sMinSeed - baseS;
    double lMaxSeed = Math.min(1, baseL + seed / 2);
    double lMinSeed = Math.max(0, lMaxSeed - seed);
    double lSeed = lMinSeed - baseL;

    int colorIndex = 0;
    for (int groupIndex = 0; groupIndex < scheme.size(); groupIndex++) {
      Group group = scheme.get(groupIndex);
      for (int colorCount = 0; colorCount < counts.get(groupIndex); colorCount++) {
        double h = group.h().apply(baseH, hSeed);
        if (h < 0) {
          h += 1;
        }
        if (h > 1) {
          h -= 1;
        }
        hSeed += seedStep;

        double s = clamp01(group.s().apply(baseS, sSeed));
        sSeed += seedStep;

        double l = clamp01(group.l().apply(baseL, lSeed));
        lSeed += seedStep;

        Map<String, Object> next = colorMe(ColorQuery.hslFraction(h, s, l));
        colors.set(colorIndex, next);
        colorIndex += 1;
      }
    }

    return colors;
  }

  private static List<Group> schemeDefinition(String mode) {
    List<Group> list = switch (mode) {
      case "monochrome" -> List.of(new Group(1.0,
          (value, seed) -> value,
          (value, seed) -> value + seed * 0.1,
          Double::sum));
      case "monochrome-light" -> List.of(
          new Group(0.4, (value, seed) -> 0d, (value, seed) -> 0d, Double::sum),
          new Group(0.6, (value, seed) -> value, (value, seed) -> value + seed * 0.1, Double::sum));
      case "monochrome-dark" -> List.of(
          new Group(0.6, (value, seed) -> value, (value, seed) -> value + seed * 0.1, Double::sum),
          new Group(0.4, (value, seed) -> 0d, (value, seed) -> 0d, Double::sum));
      case "analogic" -> List.of(new Group(1.0,
          (value, seed) -> value + seed * 0.5,
          (value, seed) -> value + seed * 0.1,
          (value, seed) -> value + seed * 0.1));
      case "complement" -> List.of(
          new Group(0.4,
              (value, seed) -> value + 0.5,
              (value, seed) -> value + seed * 0.25,
              (value, seed) -> value + seed * 0.25),
          new Group(0.6,
              (value, seed) -> value,
              (value, seed) -> value + seed * 0.25,
              (value, seed) -> value + seed * 0.25));
      case "analogic-complement" -> List.of(
          new Group(0.4,
              (value, seed) -> value + 0.5,
              (value, seed) -> value + seed * 0.5,
              (value, seed) -> value + seed * 0.5),
          new Group(0.6,
              (value, seed) -> value + seed * 0.75,
              (value, seed) -> value + seed * 0.1,
              (value, seed) -> value + seed * 0.1));
      case "triad" -> List.of(
          new Group(0.25,
              (value, seed) -> value + 0.33,
              (value, seed) -> value + seed * 0.25,
              (value, seed) -> value + seed * 0.25),
          new Group(0.25,
              (value, seed) -> value - 0.33,
              (value, seed) -> value + seed * 0.25,
              (value, seed) -> value + seed * 0.25),
          new Group(0.5,
              (value, seed) -> value,
              (value, seed) -> value + seed * 0.25,
              (value, seed) -> value + seed * 0.25));
      case "quad" -> List.of(
          new Group(0.2,
              (value, seed) -> value + 0.25,
              (value, seed) -> value + seed * 0.25,
              (value, seed) -> value + seed * 0.25),
          new Group(0.2,
              (value, seed) -> value + 0.5,
              (value, seed) -> value + seed * 0.25,
              (value, seed) -> value + seed * 0.25),
          new Group(0.2,
              (value, seed) -> value - 0.25,
              (value, seed) -> value + seed * 0.25,
              (value, seed) -> value + seed * 0.25),
          new Group(0.4,
              (value, seed) -> value,
              (value, seed) -> value + seed * 0.25,
              (value, seed) -> value + seed * 0.25));
      case "advanced" -> List.of(
          new Group(0.20, (v, s) -> v, (v, s) -> v + s * 0.15, (v, s) -> v + s * 0.35),
          new Group(0.20, (v, s) -> v + 0.08, (v, s) -> v + s * 0.25, (v, s) -> v + s * 0.20),
          new Group(0.20, (v, s) -> v + 0.50, (v, s) -> v + s * 0.30, (v, s) -> v + s * 0.10),
          new Group(0.20, (v, s) -> v - 0.08, (v, s) -> v + s * 0.20, (v, s) -> v + s * 0.25),
          new Group(0.20, (v, s) -> v + 0.25, (v, s) -> v + s * 0.15, (v, s) -> v + s * 0.30));
      default -> throw new IllegalArgumentException("Unsupported mode: " + mode);
    };
    return new ArrayList<>(list);
  }

  private static String hexCheck(String color) {
    String c = color.toUpperCase(Locale.ROOT);
    if (c.length() < 3 || c.length() > 7) {
      return "#000000";
    }
    if (c.length() % 3 == 0) {
      c = "#" + c;
    }
    if (c.length() == 4) {
      c = "#" + c.charAt(1) + c.charAt(1) + c.charAt(2) + c.charAt(2) + c.charAt(3) + c.charAt(3);
    }
    return c;
  }

  private static int[] hexToRgb(String color, boolean frac) {
    int r = Integer.parseInt(color.substring(1, 3), 16);
    int g = Integer.parseInt(color.substring(3, 5), 16);
    int b = Integer.parseInt(color.substring(5, 7), 16);
    return new int[]{r, g, b};
  }

  private static double[] rgbToHsl(Map<String, Object> values) {
    double r = num(values.get("r"));
    double g = num(values.get("g"));
    double b = num(values.get("b"));
    double min = Math.min(r, Math.min(g, b));
    double max = Math.max(r, Math.max(g, b));
    double delta = max - min;
    double l = (max + min) / 2d;
    double h = 0;
    double s = 0;
    if (delta > 0) {
      s = delta / (l < 0.5 ? (max + min) : (2 - max - min));
      double d2 = delta / 2d;
      double dr = (((max - r) / 6d) + d2) / delta;
      double dg = (((max - g) / 6d) + d2) / delta;
      double db = (((max - b) / 6d) + d2) / delta;
      if (r == max) {
        h = db - dg;
      } else if (g == max) {
        h = (1d / 3d) + dr - db;
      } else {
        h = (2d / 3d) + dg - dr;
      }
      if (h < 0) {
        h += 1;
      }
      if (h >= 1) {
        h -= 1;
      }
    }
    return new double[]{h, s, l};
  }

  private static double[] hslToRgb(double h, double s, double l) {
    h = ((h % 1) + 1) % 1;
    double H = h * 6.0;
    double C = (1 - Math.abs(2 * l - 1)) * s;
    double X = C * (1 - Math.abs(H % 2 - 1));
    double r = 0, g = 0, b = 0;
    if ((0 <= H && H < 1) || (5 <= H && H < 6)) {
      r = C;
    }
    if ((1 <= H && H < 2) || (4 <= H && H < 5)) {
      r = X;
    }
    if (1 <= H && H < 3) {
      g = C;
    }
    if ((0 <= H && H < 1) || (3 <= H && H < 4)) {
      g = X;
    }
    if (3 <= H && H < 5) {
      b = C;
    }
    if ((2 <= H && H < 3) || (5 <= H && H < 6)) {
      b = X;
    }
    double m = l - 0.5 * C;
    return new double[]{clamp01(r + m), clamp01(g + m), clamp01(b + m)};
  }

  private static double[] hsvToRgb(double h, double s, double v) {
    h = ((h % 1) + 1) % 1;
    double H = h * 6;
    double r = v, g = v, b = v;
    if (s > 0) {
      long vi = Math.round(H);
      double v1 = v * (1 - s);
      double v2 = v * (1 - s * (H - vi));
      double v3 = v * (1 - s * (1 - (H - vi)));
      if ((0 <= H && H < 1) || (5 <= H && H < 6)) {
        r = v;
      }
      if (2 <= H && H < 4) {
        r = v1;
      }
      if (1 <= H && H < 2) {
        r = v2;
      }
      if (4 <= H && H < 5) {
        r = v3;
      }

      if (1 <= H && H < 3) {
        g = v;
      }
      if (4 <= H && H < 6) {
        g = v1;
      }
      if (3 <= H && H < 4) {
        g = v2;
      }
      if (0 <= H && H < 1) {
        g = v3;
      }

      if (3 <= H && H < 5) {
        b = v;
      }
      if (0 <= H && H < 2) {
        b = v1;
      }
      if (5 <= H && H < 6) {
        b = v2;
      }
      if (2 <= H && H < 3) {
        b = v3;
      }
    }
    return new double[]{clamp01(r), clamp01(g), clamp01(b)};
  }

  private static double[] rgbToHsv(Map<String, Object> values) {
    double r = num(values.get("r"));
    double g = num(values.get("g"));
    double b = num(values.get("b"));

    double a = Math.min(r, Math.min(g, b));
    double z = Math.max(r, Math.max(g, b));
    double d = z - a;
    double v = z;
    double h = 0;
    double s = 0;

    if (d > 0) {
      s = d / z;
      double d2 = d / 2;
      double dr = (((z - r) / 6) + d2) / d;
      double dg = (((z - g) / 6) + d2) / d;
      double db = (((z - b) / 6) + d2) / d;
      if (r == z) {
        h = db - dg;
      } else if (g == z) {
        h = (1d / 3d) + dr - db;
      } else {
        h = (2d / 3d) + dg - dr;
      }
      if (h < 0) {
        h += 1;
      }
      if (h >= 1) {
        h -= 1;
      }
    }
    return new double[]{h, s, v};
  }

  private static double[] cmykToRgb(double c, double m, double y, double k) {
    double cc = c * (1 - k) + k;
    double mm = m * (1 - k) + k;
    double yy = y * (1 - k) + k;
    return new double[]{1 - cc, 1 - mm, 1 - yy};
  }

  private static double[] rgbToCmyk(Map<String, Object> values) {
    double r = num(values.get("r"));
    double g = num(values.get("g"));
    double b = num(values.get("b"));
    double c = 1 - r;
    double m = 1 - g;
    double y = 1 - b;
    double k = Math.min(1, Math.min(c, Math.min(m, y)));
    if (k > 0.997) {
      c = m = y = 0;
    }
    if (k > 0.003) {
      c = (c - k) / (1 - k);
      m = (m - k) / (1 - k);
      y = (y - k) / (1 - k);
    }
    return new double[]{clamp01(c), clamp01(m), clamp01(y), clamp01(k)};
  }

  private static double[] rgbToXyz(Map<String, Object> values) {
    double r = num(values.get("r"));
    double g = num(values.get("g"));
    double b = num(values.get("b"));

    r = r > 0.04045 ? Math.pow((r + 0.055) / 1.055, 2.4) : r / 12.92;
    g = g > 0.04045 ? Math.pow((g + 0.055) / 1.055, 2.4) : g / 12.92;
    b = b > 0.04045 ? Math.pow((b + 0.055) / 1.055, 2.4) : b / 12.92;

    double x = r * 0.4124 + g * 0.3576 + b * 0.1805;
    double y = r * 0.2126 + g * 0.7152 + b * 0.0722;
    double z = r * 0.0193 + g * 0.1192 + b * 0.9505;

    return new double[]{clamp(x, 0, 0.95047), clamp(y, 0, 1), clamp(z, 0, 1.08883)};
  }

  private static NameResult nearestNamedHex(String color) {
    String c = hexCheck(color);
    int[] rgb = hexToRgb(c, false);
    int[] hsl = hexToHslInt(c);

    for (NamedColor named : NAMED_COLORS) {
      if (("#" + named.hex).equals(c)) {
        return new NameResult("#" + named.hex, named.name, true, 0d);
      }
    }

    int cl = -1;
    double df = -1;
    for (int i = 0; i < NAMED_COLORS.size(); i++) {
      NamedColor n = NAMED_COLORS.get(i);
      double ndf1 = Math.pow(rgb[0] - n.r, 2) + Math.pow(rgb[1] - n.g, 2) + Math.pow(rgb[2] - n.b, 2);
      double ndf2 = Math.pow(hsl[0] - n.h, 2) + Math.pow(hsl[1] - n.s, 2) + Math.pow(hsl[2] - n.l, 2);
      double ndf = ndf1 + ndf2 * 2;
      if (df < 0 || df > ndf) {
        df = ndf;
        cl = i;
      }
    }

    if (cl < 0) {
      return new NameResult("#000000", "Invalid Color: " + c, false, 0d);
    }
    NamedColor best = NAMED_COLORS.get(cl);
    return new NameResult("#" + best.hex, best.name, false, df);
  }

  private static int[] hexToHslInt(String color) {
    int[] rgb = hexToRgb(color, false);
    double r = rgb[0] / 255d;
    double g = rgb[1] / 255d;
    double b = rgb[2] / 255d;

    double min = Math.min(r, Math.min(g, b));
    double max = Math.max(r, Math.max(g, b));
    double delta = max - min;
    double l = (min + max) / 2d;
    double s = 0;
    if (l > 0 && l < 1) {
      s = delta / (l < 0.5 ? (2 * l) : (2 - 2 * l));
    }
    double h = 0;
    if (delta > 0) {
      if (max == r && max != g) {
        h += (g - b) / delta;
      }
      if (max == g && max != b) {
        h += 2 + (b - r) / delta;
      }
      if (max == b && max != r) {
        h += 4 + (r - g) / delta;
      }
      h /= 6;
    }
    return new int[]{(int) (h * 360), (int) (s * 100), (int) (l * 100)};
  }

  private static double contrastRatioRgb(int[] foreground, int[] background) {
    double lum1 = luminanceRgb(foreground);
    double lum2 = luminanceRgb(background);
    if (lum1 > lum2) {
      return (lum1 + 0.05) / (lum2 + 0.05);
    }
    return (lum2 + 0.05) / (lum1 + 0.05);
  }

  private static double luminanceRgb(int[] rgb) {
    double[] lum = new double[3];
    for (int i = 0; i < 3; i++) {
      double chan = rgb[i] / 255d;
      lum[i] = (chan <= 0.03928) ? chan / 12.92 : Math.pow(((chan + 0.055) / 1.055), 2.4);
    }
    return 0.2126 * lum[0] + 0.7152 * lum[1] + 0.0722 * lum[2];
  }

  private static int[] highestContrast(int[] fg1, int[] fg2, int[] bg) {
    double c1 = contrastRatioRgb(fg1, bg);
    double c2 = contrastRatioRgb(fg2, bg);
    return c1 >= c2 ? fg1 : fg2;
  }

  private static String rgbToHex(int r, int g, int b) {
    return "#" + componentToHex(r) + componentToHex(g) + componentToHex(b);
  }

  private static String rgbFracToHex(double r, double g, double b) {
    return rgbToHex((int) Math.round(clamp01(r) * 255), (int) Math.round(clamp01(g) * 255), (int) Math.round(clamp01(b) * 255));
  }

  private static String componentToHex(int c) {
    int value = Math.max(0, Math.min(255, c));
    String hex = Integer.toHexString(value).toUpperCase(Locale.ROOT);
    return hex.length() == 1 ? "0" + hex : hex;
  }

  private static double clamp01(double v) {
    return clamp(v, 0, 1);
  }

  private static double clamp(double v, double min, double max) {
    return Math.max(min, Math.min(max, v));
  }

  private static String escapeXml(String value) {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> map(Object value) {
    return (Map<String, Object>) value;
  }

  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> listOfMaps(Object value) {
    return (List<Map<String, Object>>) value;
  }

  private static double num(Object value) {
    return ((Number) value).doubleValue();
  }

  private static String str(Object value) {
    return String.valueOf(value);
  }

  private static List<NamedColor> loadNamedColors() {
    try (InputStream in = ColorEngine.class.getClassLoader().getResourceAsStream("colorNames.json")) {
      if (in == null) {
        throw new IllegalStateException("Missing colorNames.json resource");
      }
      String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      Pattern pattern = Pattern.compile("\\{\\\"hex\\\":\\\"([0-9A-Fa-f]{6})\\\",\\\"name\\\":\\\"(.*?)\\\",\\\"r\\\":(-?\\d+),\\\"g\\\":(-?\\d+),\\\"b\\\":(-?\\d+),\\\"h\\\":(-?\\d+),\\\"s\\\":(-?\\d+),\\\"l\\\":(-?\\d+)\\}");
      Matcher matcher = pattern.matcher(json);
      List<NamedColor> results = new ArrayList<>();
      while (matcher.find()) {
        results.add(new NamedColor(
            matcher.group(1).toUpperCase(Locale.ROOT),
            unescapeJson(matcher.group(2)),
            Integer.parseInt(matcher.group(3)),
            Integer.parseInt(matcher.group(4)),
            Integer.parseInt(matcher.group(5)),
            Integer.parseInt(matcher.group(6)),
            Integer.parseInt(matcher.group(7)),
            Integer.parseInt(matcher.group(8))));
      }
      if (results.isEmpty()) {
        throw new IllegalStateException("No named colors loaded");
      }
      return results;
    } catch (IOException e) {
      throw new IllegalStateException("Unable to load color names", e);
    }
  }

  private static String unescapeJson(String value) {
    return value
        .replace("\\\\\"", "\"")
        .replace("\\\\/", "/")
        .replace("\\\\\\\\", "\\");
  }

  private record NamedColor(String hex, String name, int r, int g, int b, int h, int s, int l) {
  }

  private record NameResult(String hex, String name, boolean exact, double distance) {
  }

  private interface ModeFn {
    double apply(double value, double seed);
  }

  private record Group(double ratio, ModeFn h, ModeFn s, ModeFn l) {
  }
}
