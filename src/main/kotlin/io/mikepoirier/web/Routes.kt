package io.mikepoirier.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class Routes(
    val hiHandler: HiHandler,
    val cartHandler: CartHandler
) {

    @Bean
    fun apiRoutes() = router {
        (accept(MediaType.APPLICATION_JSON) and "/api").nest {
            "/hi".nest {
                GET("/", hiHandler::handleGet)
                POST("/", hiHandler::handlePost)
            }
        }
        accept(MediaType.APPLICATION_JSON).nest {
            "/cart".nest {
                GET("/{cartId}", cartHandler::handleGet)
                POST("/", cartHandler::handleCreateNewCart)
                POST("/{cartId}", cartHandler::handlePostSingle)
                DELETE("/{cartId}/item/{itemId}", cartHandler::handleRemoveItem)
                DELETE("/{cartId}", cartHandler::handleRemoveCart)
            }
        }
    }
}