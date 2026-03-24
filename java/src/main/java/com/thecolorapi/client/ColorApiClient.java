package com.thecolorapi.client;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ColorApiClient {
  public static final URI DEFAULT_BASE_URI = URI.create("https://www.thecolorapi.com");

  private final HttpClient httpClient;
  private final URI baseUri;

  public ColorApiClient() {
    this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), DEFAULT_BASE_URI);
  }

  public ColorApiClient(HttpClient httpClient, URI baseUri) {
    this.httpClient = Objects.requireNonNull(httpClient, "httpClient is required");
    this.baseUri = Objects.requireNonNull(baseUri, "baseUri is required");
  }

  public ColorApiResponse identify(ColorQuery colorQuery) throws IOException, InterruptedException {
    return identify(colorQuery, "json");
  }

  public ColorApiResponse identifyByHex(String hex) throws IOException, InterruptedException {
    return identify(ColorQuery.hex(hex));
  }

  public ColorApiResponse identifyByRgb(int r, int g, int b) throws IOException, InterruptedException {
    return identify(ColorQuery.rgb(r, g, b));
  }

  public ColorApiResponse identifyByHsl(int h, int s, int l) throws IOException, InterruptedException {
    return identify(ColorQuery.hsl(h, s, l));
  }

  public ColorApiResponse identifyByHsv(int h, int s, int v) throws IOException, InterruptedException {
    return identify(ColorQuery.hsv(h, s, v));
  }

  public ColorApiResponse identifyByCmyk(int c, int m, int y, int k) throws IOException, InterruptedException {
    return identify(ColorQuery.cmyk(c, m, y, k));
  }

  public ColorApiResponse identify(ColorQuery colorQuery, String format) throws IOException, InterruptedException {
    validateColorQuery(colorQuery);
    Map<String, String> params = new LinkedHashMap<>(colorQuery.toParams());
    maybeAddFormat(params, format);
    return get("/id", params);
  }

  public ColorApiResponse scheme(ColorQuery colorQuery, SchemeMode mode, int count) throws IOException, InterruptedException {
    return scheme(colorQuery, mode, count, "json");
  }

  public ColorApiResponse schemeByHex(String hex, SchemeMode mode, int count) throws IOException, InterruptedException {
    return scheme(ColorQuery.hex(hex), mode, count);
  }

  public ColorApiResponse scheme(ColorQuery colorQuery, SchemeMode mode, int count, String format) throws IOException, InterruptedException {
    validateColorQuery(colorQuery);
    if (count <= 0) {
      throw new IllegalArgumentException("count must be greater than 0");
    }
    Objects.requireNonNull(mode, "mode is required");
    Map<String, String> params = new LinkedHashMap<>(colorQuery.toParams());
    params.put("mode", mode.value());
    params.put("count", String.valueOf(count));
    maybeAddFormat(params, format);
    return get("/scheme", params);
  }

  public URI randomColorUri() {
    return baseUri.resolve("/random");
  }

  public URI colorBoxUri(ColorQuery colorQuery, Integer width, Integer height, Boolean named) {
    validateColorQuery(colorQuery);
    Map<String, String> params = new LinkedHashMap<>(colorQuery.toParams());
    maybeAddDimension(params, "w", width);
    maybeAddDimension(params, "h", height);
    if (named != null) {
      params.put("named", String.valueOf(named));
    }
    return buildUri("/colorbox", params);
  }

  public URI schemeBoxUri(ColorQuery colorQuery, SchemeMode mode, Integer count, Integer width, Integer height, Boolean named) {
    validateColorQuery(colorQuery);
    Objects.requireNonNull(mode, "mode is required");
    Map<String, String> params = new LinkedHashMap<>(colorQuery.toParams());
    params.put("mode", mode.value());
    if (count != null) {
      if (count <= 0) {
        throw new IllegalArgumentException("count must be greater than 0");
      }
      params.put("count", String.valueOf(count));
    }
    maybeAddDimension(params, "w", width);
    maybeAddDimension(params, "h", height);
    if (named != null) {
      params.put("named", String.valueOf(named));
    }
    return buildUri("/schemebox", params);
  }

  public ColorApiResponse get(String path, Map<String, String> queryParams) throws IOException, InterruptedException {
    URI uri = buildUri(path, queryParams);
    HttpRequest request = HttpRequest.newBuilder(uri)
        .GET()
        .timeout(Duration.ofSeconds(20))
        .header("Accept", "application/json")
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    return new ColorApiResponse(response.statusCode(), response.body(), uri);
  }

  public URI buildUri(String path, Map<String, String> queryParams) {
    Objects.requireNonNull(path, "path is required");
    StringBuilder sb = new StringBuilder();
    if (queryParams != null && !queryParams.isEmpty()) {
      boolean first = true;
      for (Map.Entry<String, String> entry : queryParams.entrySet()) {
        if (entry.getValue() == null) {
          continue;
        }
        if (first) {
          sb.append('?');
          first = false;
        } else {
          sb.append('&');
        }
        sb.append(urlEncode(entry.getKey())).append('=').append(urlEncode(entry.getValue()));
      }
    }

    URI basePath = baseUri.resolve(path.startsWith("/") ? path : "/" + path);
    if (sb.isEmpty()) {
      return basePath;
    }
    return URI.create(basePath + sb.toString());
  }

  private static void validateColorQuery(ColorQuery colorQuery) {
    Objects.requireNonNull(colorQuery, "colorQuery is required");
    if (colorQuery.isEmpty()) {
      throw new IllegalArgumentException("At least one color input is required (hex, rgb, hsl, cmyk, hsv)");
    }
  }

  private static void maybeAddFormat(Map<String, String> params, String format) {
    if (format != null && !format.isBlank()) {
      params.put("format", format);
    }
  }

  private static void maybeAddDimension(Map<String, String> params, String key, Integer value) {
    if (value == null) {
      return;
    }
    if (value <= 0) {
      throw new IllegalArgumentException(key + " must be greater than 0");
    }
    params.put(key, String.valueOf(value));
  }

  private static String urlEncode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
