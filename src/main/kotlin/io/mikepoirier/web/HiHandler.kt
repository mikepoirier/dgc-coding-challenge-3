package io.mikepoirier.web

import io.mikepoirier.WebException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class HiHandler {
    fun handleGet(req: ServerRequest): Mono<ServerResponse> {
        val response = Mono.just(req)
            .map {
                val name = req.queryParam("name").orElse("")
                if (name != "Mike") {
                    throw WebException(HttpStatus.BAD_REQUEST, ErrorResponse("Rude API", "I'm not talking to you!"))
                }

                GreetingResponse("Hello, $name.")
            }

        return createOkResponse(response)
    }

    fun handlePost(req: ServerRequest): Mono<ServerResponse> {
        return createOkResponse(Mono.error(NullPointerException("some message for exception")))
    }
}

data class GreetingResponse(val greeting: String)

data class ErrorResponse(val error: String?, val message: String?)