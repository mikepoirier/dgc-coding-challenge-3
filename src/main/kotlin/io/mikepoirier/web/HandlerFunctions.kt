package io.mikepoirier.web

import io.mikepoirier.WebException
import io.mikepoirier.json
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono


fun createOkResponse(success: Mono<out Any>, errorHandler: (Throwable) -> Mono<out ServerResponse> = defaultErrorHandler()): Mono<ServerResponse> {
    return success
        .flatMap { ServerResponse.ok().json().body(Mono.just(it)) }
        .onErrorResume(errorHandler)
}

fun createCreatedResponse(success: Mono<out Any>, errorHandler: (Throwable) -> Mono<out ServerResponse> = defaultErrorHandler()): Mono<ServerResponse> {
    return success
        .flatMap { ServerResponse.status(HttpStatus.CREATED).json().body(Mono.just(it)) }
        .onErrorResume(errorHandler)
}

fun defaultErrorHandler(): (Throwable) -> Mono<out ServerResponse> = {
    val log = LoggerFactory.getLogger("DefaultErrorHandler")

    log.error("Caught error", it)

    when(it::class) {
        WebException::class -> {
            it as WebException
            ServerResponse
                .status(it.httpStatus)
                .body(Mono.just(it.body))
        }
        else -> ServerResponse
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Mono.just(ErrorResponse(it::class.simpleName, it.message)))
    }
}