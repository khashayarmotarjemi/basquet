package io.khashayar.test

import io.khashayar.domain.Price
import io.khashayar.domain.Product
import io.khashayar.domain.cart.Cart
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import redis.clients.jedis.Jedis

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartTests {
    // init stuff
    private val p1 = Product(1, "first product #1", Price(100, "$"))
    private val p2 = Product(2, "first product #2", Price(200, "$"))
    private fun getNewCart(): Cart {
        return Cart(-1)
    }

    // tests
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
    fun removesItemsCorrectly() {
        Jedis().flushAll()

        val cart = getNewCart()
        cart.addItem(p1)
        cart.addItem(p1)
        cart.addItem(p2)

        cart.removeItem(p1.id)
        assertEquals(cart.getCart().count(),1)

//        assertEquals(cart.getCart().find { cartItem -> cartItem.product.id == p1.id }?.quantity,2)
//        assertEquals(cart.getCart().find { cartItem -> cartItem.product.id == p2.id }?.quantity,3)
    }

    @Test
    fun decrementsCorrectly() {
        Jedis().flushAll()

        val cart = getNewCart()
        cart.addItem(p1)
        cart.addItem(p1)
        cart.addItem(p1)

        cart.addItem(p2)
        cart.addItem(p2)
        cart.addItem(p2)
        cart.addItem(p2)

        cart.decrementOne(p1.id)
        cart.decrementOne(p1.id)

        cart.decrementOne(p2.id)

        assertEquals(cart.getCart().count(),2)
        assertEquals(cart.getCart().find { cartItem -> cartItem.product.id == p1.id }?.quantity,1)
        assertEquals(cart.getCart().find { cartItem -> cartItem.product.id == p2.id }?.quantity,3)
    }

    @Test
    fun calculatesPriceCorrectly() {
        Jedis().flushAll()

        val cart = getNewCart()

        cart.addItem(p1)
        cart.addItem(p2)
        cart.addItem(p2)

        assertEquals(cart.total().amount,500)
    }
}

