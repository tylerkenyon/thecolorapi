package com.thecolorapi.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ColorApiClientTest {

  private final ColorApiClient client = new ColorApiClient(HttpClient.newHttpClient(), URI.create("https://www.thecolorapi.com"));

  @Test
  void identifyBuildsIdUriWithHex() {
    URI uri = client.buildUri("/id", ColorQuery.hex("0047AB").toParams());
    assertEquals("https://www.thecolorapi.com/id?hex=0047AB", uri.toString());
  }

  @Test
  void identifyByHexHelperBuildsExpectedUri() {
    URI uri = client.buildUri("/id", ColorQuery.hex("24B1E0").toParams());
    assertEquals("https://www.thecolorapi.com/id?hex=24B1E0", uri.toString());
  }

  @Test
  void identifyByRgbHelperBuildsExpectedUri() {
    URI uri = client.buildUri("/id", ColorQuery.rgb(0, 71, 171).toParams());
    assertEquals("https://www.thecolorapi.com/id?rgb=0%2C71%2C171", uri.toString());
  }

  @Test
  void identifyByHslHelperBuildsExpectedUri() {
    URI uri = client.buildUri("/id", ColorQuery.hsl(215, 100, 34).toParams());
    assertEquals("https://www.thecolorapi.com/id?hsl=215%2C100%2C34", uri.toString());
  }

  @Test
  void identifyByHsvHelperBuildsExpectedUri() {
    URI uri = client.buildUri("/id", ColorQuery.hsv(215, 100, 67).toParams());
    assertEquals("https://www.thecolorapi.com/id?hsv=215%2C100%2C67", uri.toString());
  }

  @Test
  void identifyByCmykHelperBuildsExpectedUri() {
    URI uri = client.buildUri("/id", ColorQuery.cmyk(100, 58, 0, 33).toParams());
    assertEquals("https://www.thecolorapi.com/id?cmyk=100%2C58%2C0%2C33", uri.toString());
  }

  @Test
  void colorBoxUriIncludesOptionalParams() {
    URI uri = client.colorBoxUri(ColorQuery.rgb(0, 71, 171), 100, 200, true);
    assertEquals("https://www.thecolorapi.com/colorbox?rgb=0%2C71%2C171&w=100&h=200&named=true", uri.toString());
  }

  @Test
  void schemeUriIncludesModeAndCount() {
    Map<String, String> params = new LinkedHashMap<>();
    params.putAll(ColorQuery.hex("0047AB").toParams());
    params.put("mode", SchemeMode.TRIAD.value());
    params.put("count", "6");

    URI uri = client.buildUri("/scheme", params);
    assertEquals("https://www.thecolorapi.com/scheme?hex=0047AB&mode=triad&count=6", uri.toString());
  }

  @Test
  void schemeByHexHelperBuildsExpectedUri() {
    Map<String, String> params = new LinkedHashMap<>();
    params.putAll(ColorQuery.hex("0047AB").toParams());
    params.put("mode", SchemeMode.MONOCHROME.value());
    params.put("count", "5");

    URI uri = client.buildUri("/scheme", params);
    assertEquals("https://www.thecolorapi.com/scheme?hex=0047AB&mode=monochrome&count=5", uri.toString());
  }

  @Test
  void schemeRejectsInvalidCount() {
    IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
        () -> client.scheme(ColorQuery.hex("0047AB"), SchemeMode.MONOCHROME, 0));
    assertEquals("count must be greater than 0", error.getMessage());
  }

  @Test
  void colorQueryRejectsBlankHex() {
    IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
        () -> ColorQuery.hex("   "));
    assertEquals("hex is required", error.getMessage());
  }

  @Test
  void randomUriMatchesApiRoute() {
    assertEquals("https://www.thecolorapi.com/random", client.randomColorUri().toString());
  }
}
