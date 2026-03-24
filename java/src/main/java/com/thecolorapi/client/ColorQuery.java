package com.thecolorapi.client;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable input model for local color identification and scheme generation.
 *
 * <p>This type can be built directly from structured numeric channels (for example, {@link #rgb(int, int, int)})
 * or parsed from user-facing query-like strings (for example, {@code rgb(12,34,56)} and {@code FFAA11}) to mirror
 * legacy API query behavior.</p>
 */
public final class ColorQuery {
  enum Type {
    HEX,
    RGB,
    HSL,
    HSV,
    CMYK
  }

  private final Type type;
  private final String hex;
  private final Map<String, Double> values;
  private final boolean fraction;

  private ColorQuery(Type type, String hex, Map<String, Double> values, boolean fraction) {
    this.type = type;
    this.hex = hex;
    this.values = values;
    this.fraction = fraction;
  }

  /**
   * Creates a hex query.
   *
   * @param hex a 3/6 character hex value, optionally prefixed with '#'
   * @return immutable query
   */
  public static ColorQuery hex(String hex) {
    return new ColorQuery(Type.HEX, requireNonBlank(hex, "hex"), null, false);
  }

  /**
   * Creates an RGB query with integer channels in the 0..255 domain.
   */
  public static ColorQuery rgb(int r, int g, int b) {
    return new ColorQuery(Type.RGB, null, Map.of("r", (double) r, "g", (double) g, "b", (double) b), false);
  }

  /**
   * Creates an HSL query with integer channels h=0..360 and s/l=0..100.
   */
  public static ColorQuery hsl(int h, int s, int l) {
    return new ColorQuery(Type.HSL, null, Map.of("h", (double) h, "s", (double) s, "l", (double) l), false);
  }

  /**
   * Creates an HSV query with integer channels h=0..360 and s/v=0..100.
   */
  public static ColorQuery hsv(int h, int s, int v) {
    return new ColorQuery(Type.HSV, null, Map.of("h", (double) h, "s", (double) s, "v", (double) v), false);
  }

  /**
   * Creates a CMYK query with integer channels 0..100.
   */
  public static ColorQuery cmyk(int c, int m, int y, int k) {
    return new ColorQuery(Type.CMYK, null, Map.of("c", (double) c, "m", (double) m, "y", (double) y, "k", (double) k), false);
  }

  /**
   * Creates an HSL query where channel values are already fractional in the 0..1 domain.
   */
  public static ColorQuery hslFraction(double h, double s, double l) {
    return new ColorQuery(Type.HSL, null, Map.of("h", h, "s", s, "l", l), true);
  }

  /**
   * Parses query-style inputs with legacy precedence rules: rgb > hex > hsl > hsv > cmyk.
   *
   * @param hex hex input (raw or # prefixed)
   * @param rgb rgb expression (for example {@code 12,34,56} or {@code rgb(12,34,56)})
   * @param hsl hsl expression (for example {@code 220,50,40} or {@code hsl(220,50,40)})
   * @param cmyk cmyk expression (for example {@code 0,10,20,30} or {@code cmyk(0,10,20,30)})
   * @param hsv hsv expression (for example {@code 220,50,80} or {@code hsv(220,50,80)})
   * @return parsed color query
   */
  public static ColorQuery parseQueryColors(String hex, String rgb, String hsl, String cmyk, String hsv) {
    if (isPresent(rgb)) {
      return parseTriplet(Type.RGB, rgb, "r", "g", "b");
    }
    if (isPresent(hex)) {
      return hex(hex);
    }
    if (isPresent(hsl)) {
      return parseTriplet(Type.HSL, hsl, "h", "s", "l");
    }
    if (isPresent(hsv)) {
      return parseTriplet(Type.HSV, hsv, "h", "s", "v");
    }
    if (isPresent(cmyk)) {
      return parseQuad(cmyk);
    }
    throw new IllegalArgumentException("At least one color input is required (hex, rgb, hsl, cmyk, hsv)");
  }

  /**
   * Parses a generic color string using the same detection rules as legacy unknown-type parsing.
   */
  public static ColorQuery parseUnknownType(String input) {
    String raw = requireNonBlank(input, "input").trim();
    String lower = raw.toLowerCase(Locale.ROOT);
    if (lower.startsWith("cmyk")) {
      return parseQuad(raw);
    }
    if (lower.startsWith("hsl")) {
      return parseTriplet(Type.HSL, raw, "h", "s", "l");
    }
    if (lower.startsWith("hsv")) {
      return parseTriplet(Type.HSV, raw, "h", "s", "v");
    }
    if (lower.startsWith("rgb")) {
      return parseTriplet(Type.RGB, raw, "r", "g", "b");
    }
    if (raw.startsWith("#") || raw.length() == 3 || raw.length() == 6) {
      return hex(raw);
    }
    throw new IllegalArgumentException("Could not infer input type from: " + input);
  }

  Type type() {
    return type;
  }

  String hex() {
    return hex;
  }

  Map<String, Double> values() {
    return values;
  }

  boolean fraction() {
    return fraction;
  }

  private static ColorQuery parseTriplet(Type type, String raw, String k1, String k2, String k3) {
    String[] parts = normalizeParts(raw, 3);
    boolean fractional = isFractional(parts);
    Map<String, Double> parsed = Map.of(
        k1, Double.parseDouble(parts[0].trim()),
        k2, Double.parseDouble(parts[1].trim()),
        k3, Double.parseDouble(parts[2].trim()));
    return new ColorQuery(type, null, parsed, fractional);
  }

  private static ColorQuery parseQuad(String raw) {
    String[] parts = normalizeParts(raw, 4);
    boolean fractional = isFractional(parts);
    return new ColorQuery(Type.CMYK, null, Map.of(
        "c", Double.parseDouble(parts[0].trim()),
        "m", Double.parseDouble(parts[1].trim()),
        "y", Double.parseDouble(parts[2].trim()),
        "k", Double.parseDouble(parts[3].trim())), fractional);
  }

  private static String[] normalizeParts(String raw, int expectedCount) {
    String value = requireNonBlank(raw, "query component").trim();
    int open = value.indexOf('(');
    int close = value.lastIndexOf(')');
    if (open >= 0 && close > open) {
      value = value.substring(open + 1, close);
    }
    String[] parts = value.split(",");
    if (parts.length != expectedCount) {
      throw new IllegalArgumentException("Expected " + expectedCount + " channel values in: " + raw);
    }
    return parts;
  }

  private static boolean isFractional(String[] parts) {
    for (String part : parts) {
      if (part.contains(".")) {
        return true;
      }
    }
    return false;
  }

  private static boolean isPresent(String value) {
    return value != null && !value.isBlank();
  }

  private static String requireNonBlank(String value, String field) {
    Objects.requireNonNull(value, field + " is required");
    if (value.isBlank()) {
      throw new IllegalArgumentException(field + " is required");
    }
    return value;
  }
}
