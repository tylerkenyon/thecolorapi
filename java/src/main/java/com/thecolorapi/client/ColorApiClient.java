package com.thecolorapi.client;

import java.util.Map;

public final class ColorApiClient {

  public Map<String, Object> identify(ColorQuery colorQuery) {
    return ColorEngine.colorMe(colorQuery);
  }

  public Map<String, Object> identifyByHex(String hex) {
    return identify(ColorQuery.hex(hex));
  }

  public Map<String, Object> identifyByRgb(int r, int g, int b) {
    return identify(ColorQuery.rgb(r, g, b));
  }

  public Map<String, Object> identifyByHsl(int h, int s, int l) {
    return identify(ColorQuery.hsl(h, s, l));
  }

  public Map<String, Object> identifyByHsv(int h, int s, int v) {
    return identify(ColorQuery.hsv(h, s, v));
  }

  public Map<String, Object> identifyByCmyk(int c, int m, int y, int k) {
    return identify(ColorQuery.cmyk(c, m, y, k));
  }

  public Map<String, Object> scheme(ColorQuery colorQuery, SchemeMode mode, int count) {
    return ColorEngine.scheme(mode, count, identify(colorQuery));
  }

  public Map<String, Object> schemeByHex(String hex, SchemeMode mode, int count) {
    return scheme(ColorQuery.hex(hex), mode, count);
  }

  public Map<String, Object> advancedSchemeByHex(String hex, int count) {
    return scheme(ColorQuery.hex(hex), SchemeMode.ADVANCED, count);
  }

  public String randomHex() {
    return ColorEngine.randomHex();
  }

  public String colorBoxSvg(ColorQuery colorQuery, Integer width, Integer height, Boolean named) {
    return ColorEngine.colorBoxSvg(identify(colorQuery), width, height, named);
  }

  public String schemeBoxSvg(ColorQuery colorQuery, SchemeMode mode, Integer count, Integer width, Integer height, Boolean named) {
    int resolvedCount = count == null ? 5 : count;
    Map<String, Object> scheme = scheme(colorQuery, mode, resolvedCount);
    return ColorEngine.schemeBoxSvg(scheme, width, height, named);
  }
}
