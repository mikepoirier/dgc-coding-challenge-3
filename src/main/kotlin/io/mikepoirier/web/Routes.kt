package io.mikepoirier.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class Routes(val hiHandler: HiHandler) {

    @Bean
    fun apiRoutes() = router {
        (accept(MediaType.APPLICATION_JSON) and "/api").nest {
            "/hi".nest {
                GET("/", hiHandler::handleGet)
                POST("/", hiHandler::handlePost)
            }
        }
        accept(MediaType.APPLICATION_JSON).nest {

        }
    }
}