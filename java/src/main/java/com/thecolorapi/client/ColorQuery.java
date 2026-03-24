package com.thecolorapi.client;

import java.util.Map;
import java.util.Objects;

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

  public static ColorQuery hex(String hex) {
    return new ColorQuery(Type.HEX, requireNonBlank(hex, "hex"), null, false);
  }

  public static ColorQuery rgb(int r, int g, int b) {
    return new ColorQuery(Type.RGB, null, Map.of("r", (double) r, "g", (double) g, "b", (double) b), false);
  }

  public static ColorQuery hsl(int h, int s, int l) {
    return new ColorQuery(Type.HSL, null, Map.of("h", (double) h, "s", (double) s, "l", (double) l), false);
  }

  public static ColorQuery hsv(int h, int s, int v) {
    return new ColorQuery(Type.HSV, null, Map.of("h", (double) h, "s", (double) s, "v", (double) v), false);
  }

  public static ColorQuery cmyk(int c, int m, int y, int k) {
    return new ColorQuery(Type.CMYK, null, Map.of("c", (double) c, "m", (double) m, "y", (double) y, "k", (double) k), false);
  }

  public static ColorQuery hslFraction(double h, double s, double l) {
    return new ColorQuery(Type.HSL, null, Map.of("h", h, "s", s, "l", l), true);
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

  private static String requireNonBlank(String value, String field) {
    Objects.requireNonNull(value, field + " is required");
    if (value.isBlank()) {
      throw new IllegalArgumentException(field + " is required");
    }
    return value;
  }
}
