package com.thecolorapi.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ColorQuery {
  private final String hex;
  private final String rgb;
  private final String hsl;
  private final String cmyk;
  private final String hsv;

  private ColorQuery(String hex, String rgb, String hsl, String cmyk, String hsv) {
    this.hex = hex;
    this.rgb = rgb;
    this.hsl = hsl;
    this.cmyk = cmyk;
    this.hsv = hsv;
  }

  public static ColorQuery hex(String hex) {
    return new ColorQuery(requireNonBlank(hex, "hex"), null, null, null, null);
  }

  public static ColorQuery rgb(String rgb) {
    return new ColorQuery(null, requireNonBlank(rgb, "rgb"), null, null, null);
  }

  public static ColorQuery rgb(int r, int g, int b) {
    return rgb("%d,%d,%d".formatted(r, g, b));
  }

  public static ColorQuery hsl(String hsl) {
    return new ColorQuery(null, null, requireNonBlank(hsl, "hsl"), null, null);
  }

  public static ColorQuery hsl(int h, int s, int l) {
    return hsl("%d,%d,%d".formatted(h, s, l));
  }

  public static ColorQuery cmyk(String cmyk) {
    return new ColorQuery(null, null, null, requireNonBlank(cmyk, "cmyk"), null);
  }

  public static ColorQuery cmyk(int c, int m, int y, int k) {
    return cmyk("%d,%d,%d,%d".formatted(c, m, y, k));
  }

  public static ColorQuery hsv(String hsv) {
    return new ColorQuery(null, null, null, null, requireNonBlank(hsv, "hsv"));
  }

  public static ColorQuery hsv(int h, int s, int v) {
    return hsv("%d,%d,%d".formatted(h, s, v));
  }

  public Map<String, String> toParams() {
    Map<String, String> params = new LinkedHashMap<>();
    if (hex != null) {
      params.put("hex", hex);
    }
    if (rgb != null) {
      params.put("rgb", rgb);
    }
    if (hsl != null) {
      params.put("hsl", hsl);
    }
    if (cmyk != null) {
      params.put("cmyk", cmyk);
    }
    if (hsv != null) {
      params.put("hsv", hsv);
    }
    return params;
  }

  boolean isEmpty() {
    return hex == null && rgb == null && hsl == null && cmyk == null && hsv == null;
  }

  private static String requireNonBlank(String value, String field) {
    Objects.requireNonNull(value, field + " is required");
    if (value.isBlank()) {
      throw new IllegalArgumentException(field + " is required");
    }
    return value;
  }
}
