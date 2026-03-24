package com.thecolorapi.client;

public enum SchemeMode {
  MONOCHROME("monochrome"),
  MONOCHROME_DARK("monochrome-dark"),
  MONOCHROME_LIGHT("monochrome-light"),
  ANALOGIC("analogic"),
  COMPLEMENT("complement"),
  ANALOGIC_COMPLEMENT("analogic-complement"),
  TRIAD("triad"),
  QUAD("quad");

  private final String value;

  SchemeMode(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
