package io.khashayar.data

import io.khashayar.domain.cart.CartItem
import io.khashayar.domain.product.Price
import redis.clients.jedis.exceptions.JedisException

interface CartRepository {

    fun getItem(userId: Int, productId: Long): String?

    fun getAll(userId: Int): String?

    fun addOrUpdate(userId: Int, productId: Long, cartItemJson: String): Boolean

    fun deleteCart(userId: Int): Boolean

    fun deleteItem(userId: Int, productId: Long): Boolean

    fun hasCart(userId: Int): Boolean

    fun hasItem(userId: Int, productId: Long): Boolean

}