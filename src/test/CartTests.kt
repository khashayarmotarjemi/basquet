package io.khashayar.test

import com.beust.klaxon.Klaxon
import io.khashayar.domain.cart.CartInteractor
import io.khashayar.domain.cart.CartItem
import io.khashayar.domain.product.Price
import io.khashayar.domain.product.Product
import io.khashayar.data.CartRedisRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import redis.clients.jedis.Jedis

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartRepositoryTests {
    // init stuff
    private val p1 = Product(1, "first product #1", Price(100, "$"))
    private val p2 = Product(2, "first product #2", Price(200, "$"))
    private val repository = CartRedisRepository()

    private fun getNewCart(): CartInteractor {
        return CartInteractor(-1, repository)
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

        print(cart.itemsJson())

        assertEquals(getItemsFromJson(cart.itemsJson()).count(), 2)

        assertEquals(getItemsFromJson(cart.itemsJson()).find { cartItem -> cartItem.product.id == p1.id }?.quantity, 2)
        assertEquals(getItemsFromJson(cart.itemsJson()).find { cartItem -> cartItem.product.id == p2.id }?.quantity, 3)
    }

    private fun getItemsFromJson(json: String): List<CartItem> {
        val list: List<CartItem> = Klaxon().parseArray(json)!!
        return ArrayList(list)
    }


    @Test
    fun removesItemsCorrectly() {
        Jedis().flushAll()

        val cart = getNewCart()
        cart.addItem(p1)
        cart.addItem(p1)
        cart.addItem(p2)

        cart.deleteItem(p1.id)
        assertEquals(getItemsFromJson(cart.itemsJson()).count(), 1)

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

        cart.decrementQuantity(p1.id)
        cart.decrementQuantity(p1.id)

        cart.decrementQuantity(p2.id)

        assertEquals(getItemsFromJson(cart.itemsJson()).count(), 2)
        assertEquals(getItemsFromJson(cart.itemsJson()).find { cartItem -> cartItem.product.id == p1.id }?.quantity, 1)
        assertEquals(getItemsFromJson(cart.itemsJson()).find { cartItem -> cartItem.product.id == p2.id }?.quantity, 3)
    }

    @Test
    fun calculatesPriceCorrectly() {
        Jedis().flushAll()

        val cart = getNewCart()

        cart.addItem(p1)
        cart.addItem(p2)
        cart.addItem(p2)

        assertEquals(cart.totalPrice().amount, 500)
    }
}

