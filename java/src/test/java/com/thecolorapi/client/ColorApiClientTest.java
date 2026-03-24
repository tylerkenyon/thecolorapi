package com.thecolorapi.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ColorApiClientTest {

  private final ColorApiClient client = new ColorApiClient();

  @Test
  void identifyByHexReturnsLocalColorPayload() {
    Map<String, Object> color = client.identifyByHex("0047AB");

    Map<String, Object> hex = castMap(color.get("hex"));
    Map<String, Object> rgb = castMap(color.get("rgb"));
    Map<String, Object> name = castMap(color.get("name"));
    Map<String, Object> links = castMap(color.get("_links"));

    assertEquals("#0047AB", hex.get("value"));
    assertEquals("0047AB", hex.get("clean"));
    assertEquals(0, rgb.get("r"));
    assertEquals(71, rgb.get("g"));
    assertEquals(171, rgb.get("b"));
    assertEquals("Cobalt", name.get("value"));
    assertEquals(true, name.get("exact_match_name"));
    assertNotNull(links.get("self"));
  }

  @Test
  void identifyAcceptsAllInputModels() {
    Map<String, Object> byRgb = client.identifyByRgb(0, 71, 171);
    Map<String, Object> byHsl = client.identifyByHsl(215, 100, 34);
    Map<String, Object> byHsv = client.identifyByHsv(215, 100, 67);
    Map<String, Object> byCmyk = client.identifyByCmyk(100, 58, 0, 33);

    assertEquals("#0047AB", castMap(byRgb.get("hex")).get("value"));
    assertTrue(castMap(byHsl.get("hex")).get("value").toString().matches("^#[0-9A-F]{6}$"));
    assertTrue(castMap(byHsv.get("hex")).get("value").toString().matches("^#[0-9A-F]{6}$"));
    assertTrue(castMap(byCmyk.get("hex")).get("value").toString().matches("^#[0-9A-F]{6}$"));
  }

  @Test
  void identifyFromQueryMatchesLegacyPrecedence() {
    Map<String, Object> color = client.identifyFromQuery("0047AB", "rgb(255,0,0)", "hsl(215,100,34)", "cmyk(100,58,0,33)", "hsv(215,100,67)");
    assertEquals("#FF0000", castMap(color.get("hex")).get("value"));

    Map<String, Object> hexOnly = client.identifyFromQuery("0047AB", null, null, null, null);
    assertEquals("#0047AB", castMap(hexOnly.get("hex")).get("value"));
  }

  @Test
  void unknownTypeParsingSupportsLegacyFormats() {
    Map<String, Object> fromRgb = client.identifyUnknown("rgb(0,71,171)");
    Map<String, Object> fromHex = client.identifyUnknown("0047AB");
    Map<String, Object> fromHsl = client.identifyUnknown("hsl(215,100,34)");
    Map<String, Object> fromHsv = client.identifyUnknown("hsv(215,100,67)");
    Map<String, Object> fromCmyk = client.identifyUnknown("cmyk(100,58,0,33)");

    assertEquals("#0047AB", castMap(fromRgb.get("hex")).get("value"));
    assertEquals("#0047AB", castMap(fromHex.get("hex")).get("value"));
    assertTrue(castMap(fromHsl.get("hex")).get("value").toString().matches("^#[0-9A-F]{6}$"));
    assertTrue(castMap(fromHsv.get("hex")).get("value").toString().matches("^#[0-9A-F]{6}$"));
    assertTrue(castMap(fromCmyk.get("hex")).get("value").toString().matches("^#[0-9A-F]{6}$"));
  }

  @Test
  void schemeReturnsRequestedColorCount() {
    Map<String, Object> scheme = client.schemeByHex("0047AB", SchemeMode.TRIAD, 6);

    assertEquals("triad", scheme.get("mode"));
    assertEquals(6, scheme.get("count"));

    List<Map<String, Object>> colors = castList(scheme.get("colors"));
    assertEquals(6, colors.size());

    Map<String, Object> seed = castMap(scheme.get("seed"));
    assertEquals("#0047AB", castMap(seed.get("hex")).get("value"));

    Map<String, Object> links = castMap(scheme.get("_links"));
    Map<String, Object> schemes = castMap(links.get("schemes"));
    assertTrue(schemes.containsKey("monochrome"));
    assertTrue(schemes.containsKey("advanced"));
  }

  @Test
  void advancedSchemeProducesDiversePalette() {
    Map<String, Object> scheme = client.advancedSchemeByHex("0047AB", 8);

    assertEquals("advanced", scheme.get("mode"));
    List<Map<String, Object>> colors = castList(scheme.get("colors"));
    assertEquals(8, colors.size());

    long distinctHex = colors.stream()
        .map(c -> castMap(c.get("hex")).get("value").toString())
        .distinct()
        .count();
    assertTrue(distinctHex >= 5);
  }

  @Test
  void svgHelpersRenderLocally() {
    String colorSvg = client.colorBoxSvg(ColorQuery.hex("0047AB"), 120, 80, true);
    assertTrue(colorSvg.contains("<svg"));
    assertTrue(colorSvg.contains("#0047AB"));
    assertTrue(colorSvg.contains("Cobalt"));

    String schemeSvg = client.schemeBoxSvg(ColorQuery.hex("0047AB"), SchemeMode.MONOCHROME, 5, 120, 220, false);
    assertTrue(schemeSvg.contains("<svg"));
    assertTrue(schemeSvg.contains("<rect"));
    assertFalse(schemeSvg.contains("<text"));
  }

  @Test
  void randomHexLooksValid() {
    String hex = client.randomHex();
    assertTrue(hex.matches("^#[0-9A-F]{6}$"));
  }

  @Test
  void invalidUnknownInputThrows() {
    IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> client.identifyUnknown("not-a-color"));
    assertTrue(error.getMessage().contains("Could not infer input type"));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> castMap(Object value) {
    return (Map<String, Object>) value;
  }

  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> castList(Object value) {
    return (List<Map<String, Object>>) value;
  }
}
