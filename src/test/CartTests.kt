package io.khashayar.test

import com.beust.klaxon.Klaxon
import io.khashayar.domain.cart.CartInteractor
import io.khashayar.domain.cart.CartItem
import io.khashayar.domain.product.Price
import io.khashayar.domain.product.Product
import io.khashayar.data.CartRedisRepository
import io.khashayar.data.ProductsRedisRepository
import io.khashayar.domain.product.ProductInteractor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import redis.clients.jedis.Jedis

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartInteractorTest {
    // init stuff
    private val p1 = Product(1, "first product #1", Price(100, "$"))
    private val p2 = Product(2, "second product #2", Price(200, "$"))

    private val cartRedisRepository = CartRedisRepository()
    private val productsRedisRepository = ProductsRedisRepository()
    private val productInteractor = ProductInteractor(productsRedisRepository)


    private fun getNewCart(): CartInteractor {
        Jedis().flushAll()
        productInteractor.add(p1.name,p1.price.amount,"")
        productInteractor.add(p2.name,p2.price.amount,"")

        return CartInteractor(-1, cartRedisRepository,productInteractor)
    }

    // tests
    @Test
    fun addsItemsCorrectly() {

        val cart = getNewCart()
        cart.addItem(p1.id)
        cart.addItem(p1.id)
        cart.addItem(p2.id)
        cart.addItem(p2.id)
        cart.addItem(p2.id)

        assertEquals(2, getItemsFromJson(cart.itemsJson()).count())

        assertEquals(getItemsFromJson(cart.itemsJson()).find { cartItem -> cartItem.product.id == p1.id }?.quantity, 2)
        assertEquals(getItemsFromJson(cart.itemsJson()).find { cartItem -> cartItem.product.id == p2.id }?.quantity, 3)
    }

    private fun getItemsFromJson(json: String): List<CartItem> {
        val list: List<CartItem> = Klaxon().parseArray(json)!!
        return ArrayList(list)
    }

    @Test
    fun removesItemsCorrectly() {
        val cart = getNewCart()
        cart.addItem(p1.id)
        cart.addItem(p1.id)
        cart.addItem(p2.id)

        cart.deleteItem(p1.id)
        assertEquals(getItemsFromJson(cart.itemsJson()).count(), 1)
    }

    @Test
    fun decrementsCorrectly() {
        val cart = getNewCart()
        cart.addItem(p1.id)
        cart.addItem(p1.id)
        cart.addItem(p1.id)

        cart.addItem(p2.id)
        cart.addItem(p2.id)
        cart.addItem(p2.id)
        cart.addItem(p2.id)

        cart.decrementQuantity(p1.id)
        cart.decrementQuantity(p1.id)

        cart.decrementQuantity(p2.id)

        assertEquals(getItemsFromJson(cart.itemsJson()).count(), 2)
        assertEquals(getItemsFromJson(cart.itemsJson()).find { cartItem -> cartItem.product.id == p1.id }?.quantity, 1)
        assertEquals(getItemsFromJson(cart.itemsJson()).find { cartItem -> cartItem.product.id == p2.id }?.quantity, 3)
    }
    @Test
    fun calculatesPriceCorrectly() {
        val cart = getNewCart()

        cart.addItem(p1.id)
        cart.addItem(p2.id)
        cart.addItem(p2.id)

        assertEquals(cart.totalPrice().amount, 500)
    }
}

