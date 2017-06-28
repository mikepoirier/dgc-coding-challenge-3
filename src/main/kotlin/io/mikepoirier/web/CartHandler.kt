package io.mikepoirier.web

import io.mikepoirier.CartNotFoundException
import io.mikepoirier.WebException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.atomic.AtomicLong

@Component
class CartHandler {

    val carts = mutableMapOf<Long, MutableList<Item>>()
    val cartIdGenerator = AtomicLong(1)
    val itemIdGenerator = AtomicLong(1)

    fun handleCreateNewCart(req: ServerRequest): Mono<ServerResponse> {
        val response = Mono.just(req)
            .map {
                cartIdGenerator.getAndIncrement()
            }
            .doOnNext {
                carts.put(it, mutableListOf())
            }
            .map {
                NewResponse(it)
            }

        return createCreatedResponse(response)
    }


    fun handlePostSingle(req: ServerRequest): Mono<ServerResponse> {

        val cartId = req.pathVariable("cartId")
        val response = req.bodyToMono<Item>()
            .map {
                it.copy(id = itemIdGenerator.getAndIncrement())
            }
            .doOnNext { item ->
                val cartIdLong = cartId.toLong()

                carts[cartIdLong]?.add(item)
            }
            .map {
                NewResponse(it.id)
            }

        return createCreatedResponse(response)
    }

    fun handleRemoveItem(req: ServerRequest): Mono<ServerResponse> {

        val cartId = req.pathVariable("cartId").toLong()
        val itemId = req.pathVariable("itemId").toLong()

        val response = Mono.just("")
            .doOnNext {
                when(carts[cartId]) {
                    null -> throw WebException(HttpStatus.NOT_FOUND, "")
                    else -> {
                        carts[cartId]?.let { items ->
                            val newItems = if(items.any { it.id == itemId }) {
                                items.filter { (id) ->
                                    id != itemId
                                }
                                    .toMutableList()
                            } else {
                                throw WebException(HttpStatus.NOT_FOUND, "")
                            }

                            carts[cartId] = newItems
                        }
                    }
                }
            }

        return createOkResponse(response)
    }

    fun handleRemoveCart(req: ServerRequest): Mono<ServerResponse> {

        val response = Mono.just(req.pathVariable("cartId").toLong())
            .doOnNext { cartId ->
                when(carts[cartId]) {
                    null -> throw WebException(HttpStatus.NOT_FOUND, "")
                    else -> {
                        carts.remove(cartId)
                    }
                }
            }

        return createOkResponse(response)
    }

    fun handleGet(req: ServerRequest): Mono<ServerResponse> {

        val response = Mono.just(req.pathVariable("cartId").toLong())
            .map { cartId ->
                when(carts[cartId]) {
                    null -> throw WebException(HttpStatus.NOT_FOUND, "")
                    else -> {
                        val items = carts[cartId]!!
                        val subtotal = items.map(Item::price).sum()
                        val discount = items.groupBy(Item::sku)
                            .map { entry ->
                                val list = entry.value
                                if(list.size > 1) {
                                    val total = list.map(Item::price).sum()
                                    total.times(0.1)
                                } else {
                                    0.0
                                }
                            }
                            .sum()
                            .toBigDecimal()
                            .setScale(2, RoundingMode.HALF_UP)
                            .toDouble()
                        val total = subtotal.toBigDecimal().minus(discount.toBigDecimal()).toDouble()

                        Cart(subtotal, discount, total, items)
                    }
                }
            }

        return createOkResponse(response)
    }
}

fun Double.toBigDecimal(): BigDecimal {
    return BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP)
}

data class NewResponse(val id: Long)

data class Cart(var subTotal: Double = 0.0, val discount: Double = 0.0, val total: Double = 0.0, val items: MutableList<Item> = mutableListOf())

data class Item(val id: Long, val sku: Long, val price: Double)