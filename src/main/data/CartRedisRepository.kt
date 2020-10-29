package io.khashayar.data

import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import io.khashayar.domain.product.Price
import io.khashayar.domain.product.Product
import io.khashayar.domain.cart.CartItem
import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.JedisException

// TODO: move json parsing and conversion to the interactor

class CartRedisRepository : CartRepository {
    private val jedis: Jedis = Jedis()

    override fun hasCart(userId: Int): Boolean {
        return try {
            jedis.exists(userId.toString())
        } catch (ke: KlaxonException) {
            print(ke)
            print(ke.stackTrace)
            false
        }
    }

    override fun hasItem(userId: Int, productId: Long): Boolean {
        return try {
            jedis.hexists(userId.toString(), productId.toString())
        } catch (ke: KlaxonException) {
            print(ke)
            print(ke.stackTrace)
            false
        }
    }


    override fun getItem(userId: Int, productId: Long): String? {
        val itemJson: String
        try {
            if(hasItem(userId,productId)) {
                itemJson = jedis.hget(userId.toString(), productId.toString())
            } else {
                return null
            }
        } catch (ke: KlaxonException) {
            print(ke)
            print(ke.stackTrace)
            return null
        }
        return itemJson
    }

    override fun getAll(userId: Int): String? {
        return if (hasCart(userId)) {
            val cartData = jedis.hgetAll(userId.toString())
            return cartData.values.toString()
        } else {
            null
        }
    }

    override fun addOrUpdate(userId: Int, productId: Long, cartItemJson: String): Boolean {
        try {
            jedis.hset(userId.toString(), productId.toString(), cartItemJson)
        } catch (ke: KlaxonException) {
            print(ke)
            print(ke.stackTrace)
            return false
        }
        return true
    }

    override fun deleteCart(userId: Int): Boolean {
        if (hasCart(userId)) {
            try {
                jedis.del(userId.toString())
            } catch (ke: KlaxonException) {
                print(ke)
                print(ke.stackTrace)
                return false
            }
            return true
        } else {
            print("Database doesn't have a cart for user with id: $userId")
            return false
        }
    }

    override fun deleteItem(userId: Int, productId: Long): Boolean {
        if (hasCart(userId)) {
            try {
                jedis.hdel(userId.toString(), productId.toString())
            } catch (ke: KlaxonException) {
                print(ke)
                print(ke.stackTrace)
                return false
            }
            return true
        } else {
            print("Database doesn't have a cart for user with id: $userId")
            return false
        }
    }


    /*fun getCartJson(userId: Int): String? {
        return if (hasCart(userId)) {
            val cartData = jedis.hget("cart", userId.toString())
            cartData
        } else {
            null
        }
    }

    fun add(userId: Int, productId: Long, productJson: String) {
        try {
            if (hasCart(userId)) {
                val oldItems = getCartItems(userId)

                if (hasItem(userId, productId)) {
                    updateData(incrementItem(oldItems, productId), userId)
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
    }*/
}