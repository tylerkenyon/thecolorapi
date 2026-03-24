package com.thecolorapi.client;

import java.net.URI;

/**
 * Legacy record retained for source compatibility with earlier wrapper-based API client versions.
 *
 * <p>The local library does not require remote responses for core operations. This type remains available to
 * avoid breaking consumers that still reference it.</p>
 */
public record ColorApiResponse(int statusCode, String body, URI uri) {
}
