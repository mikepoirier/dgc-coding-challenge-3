package io.mikepoirier

import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse


fun ServerResponse.BodyBuilder.json() = contentType(MediaType.APPLICATION_JSON)
fun ServerResponse.BodyBuilder.jsonStream() = contentType(MediaType.APPLICATION_STREAM_JSON)