package com.thecolorapi.client;

/**
 * Supported scheme modes.
 *
 * <p>The string values map to route-compatible mode keys from the legacy API implementation.</p>
 */
public enum SchemeMode {
  MONOCHROME("monochrome"),
  MONOCHROME_DARK("monochrome-dark"),
  MONOCHROME_LIGHT("monochrome-light"),
  ANALOGIC("analogic"),
  COMPLEMENT("complement"),
  ANALOGIC_COMPLEMENT("analogic-complement"),
  TRIAD("triad"),
  QUAD("quad"),
  ADVANCED("advanced");

  private final String value;

  SchemeMode(String value) {
    this.value = value;
  }

  /**
   * Returns the route-compatible lowercase mode token.
   */
  public String value() {
    return value;
  }
}
