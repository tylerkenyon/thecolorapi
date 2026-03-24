package com.thecolorapi.client;

import java.util.Map;

/**
 * Main public API for the local Java color library.
 *
 * <p>This client offers local equivalents of the original service capabilities:
 * identify/id, scheme generation, colorbox/schemebox SVG output, and random color generation.
 * All operations are executed in-process with no outbound HTTP requirement.</p>
 */
public final class ColorApiClient {

  /**
   * Resolves a color payload from a structured query.
   *
   * @param colorQuery parsed color query
   * @return API-shaped color object map
   */
  public Map<String, Object> identify(ColorQuery colorQuery) {
    return ColorEngine.colorMe(colorQuery);
  }

  /**
   * Resolves color details from a hex value.
   */
  public Map<String, Object> identifyByHex(String hex) {
    return identify(ColorQuery.hex(hex));
  }

  /**
   * Resolves color details from integer RGB channels.
   */
  public Map<String, Object> identifyByRgb(int r, int g, int b) {
    return identify(ColorQuery.rgb(r, g, b));
  }

  /**
   * Resolves color details from integer HSL channels.
   */
  public Map<String, Object> identifyByHsl(int h, int s, int l) {
    return identify(ColorQuery.hsl(h, s, l));
  }

  /**
   * Resolves color details from integer HSV channels.
   */
  public Map<String, Object> identifyByHsv(int h, int s, int v) {
    return identify(ColorQuery.hsv(h, s, v));
  }

  /**
   * Resolves color details from integer CMYK channels.
   */
  public Map<String, Object> identifyByCmyk(int c, int m, int y, int k) {
    return identify(ColorQuery.cmyk(c, m, y, k));
  }

  /**
   * Resolves color details using legacy query-style input precedence.
   *
   * <p>Precedence is rgb > hex > hsl > hsv > cmyk.</p>
   */
  public Map<String, Object> identifyFromQuery(String hex, String rgb, String hsl, String cmyk, String hsv) {
    return identify(ColorQuery.parseQueryColors(hex, rgb, hsl, cmyk, hsv));
  }

  /**
   * Resolves color details from an unknown expression by auto-detecting input type.
   */
  public Map<String, Object> identifyUnknown(String input) {
    return identify(ColorQuery.parseUnknownType(input));
  }

  /**
   * Generates a scheme payload for the given seed query.
   *
   * @param colorQuery seed color query
   * @param mode scheme mode
   * @param count number of output colors
   * @return API-shaped scheme object map
   */
  public Map<String, Object> scheme(ColorQuery colorQuery, SchemeMode mode, int count) {
    return ColorEngine.scheme(mode, count, identify(colorQuery));
  }

  /**
   * Convenience overload for hex seed scheme generation.
   */
  public Map<String, Object> schemeByHex(String hex, SchemeMode mode, int count) {
    return scheme(ColorQuery.hex(hex), mode, count);
  }

  /**
   * High-variety scheme generation using the advanced mode.
   */
  public Map<String, Object> advancedSchemeByHex(String hex, int count) {
    return scheme(ColorQuery.hex(hex), SchemeMode.ADVANCED, count);
  }

  /**
   * Returns a locally generated random hexadecimal color string.
   */
  public String randomHex() {
    return ColorEngine.randomHex();
  }

  /**
   * Creates a local SVG color swatch equivalent to colorbox output.
   *
   * @param colorQuery color input
   * @param width optional width, defaults to 100
   * @param height optional height, defaults to 100
   * @param named whether name text should be rendered (null treated as true)
   * @return SVG string
   */
  public String colorBoxSvg(ColorQuery colorQuery, Integer width, Integer height, Boolean named) {
    return ColorEngine.colorBoxSvg(identify(colorQuery), width, height, named);
  }

  /**
   * Creates a local SVG scheme equivalent to schemebox output.
   *
   * @param colorQuery seed input
   * @param mode scheme mode
   * @param count optional count, defaults to 5
   * @param width optional width, defaults to 100
   * @param height optional height, defaults to 200
   * @param named whether color names should be rendered (null treated as true)
   * @return SVG string
   */
  public String schemeBoxSvg(ColorQuery colorQuery, SchemeMode mode, Integer count, Integer width, Integer height, Boolean named) {
    int resolvedCount = count == null ? 5 : count;
    Map<String, Object> scheme = scheme(colorQuery, mode, resolvedCount);
    return ColorEngine.schemeBoxSvg(scheme, width, height, named);
  }
}
