package io.mikepoirier

import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono


data class WebException(val httpStatus: HttpStatus, val body: Any): Throwable("WebException")