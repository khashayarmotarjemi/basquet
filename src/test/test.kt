package io.khashayar.test

import io.khashayar.domain.Price
import io.khashayar.domain.Product
import io.khashayar.domain.cart.Cart
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartTests {
    val cart = Cart()

    val p1 = Product(1, "first product #1", Price(100));
    val p2 = Product(2, "first product #2", Price(200));

    init {
        cart.addItem(p1)
        cart.addItem(p2)
    }

    @Test
    fun addsItemsCorrectly() {
        assertEquals(2, cart.count())
    }

    @Test
    fun calculatesTheTotalCorrectly() {
        assertEquals(300, cart.total().amount)
    }

    @Test
    fun removesAnItemCorrectly() {
        cart.removeItem(p1.id)
    }
}

