package io.khashayar.data

import com.beust.klaxon.Klaxon
import io.khashayar.domain.product.Price
import io.khashayar.domain.product.Product
import io.khashayar.domain.cart.CartItem
import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.JedisException

// TODO: move json parsing and conversion to the interacto

class CartRedisRepository() {
    private val jedis: Jedis = Jedis()
    private val klx: Klaxon = Klaxon()

    private fun updateData(items: List<CartItem>, userId: Int) {
        jedis.hset("cart", userId.toString(), klx.toJsonString(items))
    }

    private fun getCartItems(userId: Int): ArrayList<CartItem> {
        return try {
            val productsList = getCartJson(userId)
            if (productsList != null) {
                val list: List<CartItem> = klx.parseArray(productsList)!!
                ArrayList(list)
            } else {
                ArrayList()
            }
        } catch (e: Exception) {
            print(e)
            print(e.stackTrace)
            ArrayList()
        }
    }

    private fun hasCart(userId: Int): Boolean {
        return jedis.hexists("cart", userId.toString())
    }

    private fun hasItem(items: List<CartItem>, id: Long): Boolean {
        return items.map { item -> item.product.id }.contains(id)
    }

    private fun incrementItem(items: List<CartItem>, id: Long): List<CartItem> {
        return items.map { cartItem ->
            if (cartItem.product.id == id) {
                cartItem.incrementQuantity()
            }
            cartItem
        }
    }


    fun getCartJson(userId: Int): String? {
        return if (hasCart(userId)) {
            val cartData = jedis.hget("cart", userId.toString())
            cartData
        } else {
            null
        }
    }

    fun add(userId: Int, product: Product) {
        try {
            if (hasCart(userId)) {
                val oldItems = getCartItems(userId)

                if (hasItem(oldItems, product.id)) {
                    updateData(incrementItem(oldItems, product.id), userId)
                } else {
                    oldItems.add(CartItem(product, 1))
                    updateData(oldItems, userId)
                }
            } else {
                updateData(arrayListOf(CartItem(product, 1)), userId)
            }
        } catch (je: JedisException) {
            print(je)
            print(je.stackTrace)
        }
    }

    fun deleteCart(userId: Int) {
        jedis.hdel("cart", userId.toString())
    }

    fun removeItem(userId: Int, productId: Long) {
        val items = getCartItems(userId)
        if (hasItem(items, productId)) {
            items.removeIf { item -> item.product.id == productId }
            updateData(items, userId)
        } else {
            print("Item to delete with id $productId doesn't exist")
        }
    }

    fun decrementOne(userId: Int, productId: Long) {
        val items = getCartItems(userId)
        if (hasItem(items, productId)) {
            if (items.find { item -> item.product.id == productId }?.quantity == 1) {
                items.removeIf { item -> item.product.id == productId }
                updateData(items, userId)
            } else {
                val newItems: List<CartItem> = items.map { cartItem ->
                    if (cartItem.product.id == productId) {
                        cartItem.decrementQuantity()
                    }
                    cartItem
                }
                updateData(newItems, userId)
            }
        } else {
            print("Item to delete with id $productId doesn't exist")
        }
    }

    fun total(userId: Int): Price {
        return getCartItems(userId).fold(
            initial = Price(0, "$"),
            operation = { prev, item -> prev.plus(item.product.price.amount * item.quantity) })
    }
}