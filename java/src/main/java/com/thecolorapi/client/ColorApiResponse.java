package com.thecolorapi.client;

import java.net.URI;

public record ColorApiResponse(int statusCode, String body, URI uri) {
}
