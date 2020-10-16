package io.khashayar.test

import com.beust.klaxon.Klaxon
import io.khashayar.domain.Price
import io.khashayar.domain.Product
import io.khashayar.domain.cart.Cart
import io.khashayar.domain.cart.CartItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import redis.clients.jedis.Jedis

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartTests {
    lateinit var cart: Cart
    private val p1 = Product(1, "first product #1", Price(100, "$"))
    private val p2 = Product(2, "first product #2", Price(200, "$"))


    @Test
    fun addsItemsCorrectly() {
        Jedis().flushAll()

        val cart = getNewCart()
        cart.addItem(p1)
        cart.addItem(p1)
        cart.addItem(p2)
        cart.addItem(p2)
        cart.addItem(p2)

        assertEquals(cart.getCart().count(),2)
        assertEquals(cart.getCart().find { cartItem -> cartItem.product.id == p1.id }?.quantity,2)
        assertEquals(cart.getCart().find { cartItem -> cartItem.product.id == p2.id }?.quantity,3)
    }

    @Test
    fun calculatesTheTotalCorrectly() {
        assertEquals(300, cart.total().amount)
    }

    fun getNewCart(): Cart {
        return Cart(-1)
    }

    @Test
    fun check() {

    }

    @Test
    fun getCart() {
        Jedis().flushAll()

        val list = ArrayList<CartItem>()
        list.add(CartItem(p1, 1))
        list.add(CartItem(p1, 2))

        print(Klaxon().toJsonString(list))
    }
}

