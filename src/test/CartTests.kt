package io.khashayar.test

import com.beust.klaxon.Klaxon
import io.khashayar.data.CartRedisRepository
import io.khashayar.domain.product.Price
import io.khashayar.domain.product.Product
import io.khashayar.data.CartRepository
import io.khashayar.domain.cart.CartItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import redis.clients.jedis.Jedis
import kotlin.test.assertNotNull


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class CartRepositoryTests {
    // init stuff
    private val klx = Klaxon()

    private val userId = 999

    private val p1 = Product(1, "first product #1", Price(100, "$"))
    private val p2 = Product(2, "second product #2", Price(200, "$"))
    private val p3 = Product(3, "third product #3", Price(300, "$"))
    private val np4 = Product(4, "third product #4", Price(400, "$"))

    private val c1 = CartItem(p1, 1)
    private val c2 = CartItem(p2, 1)
    private val c3 = CartItem(p3, 1)
    private val nc4 = CartItem(np4, 1)

    private val c1json = klx.toJsonString(c1)
    private val c2json = klx.toJsonString(c2)
    private val c3json = klx.toJsonString(c3)
    private val nc4json = klx.toJsonString(nc4)


    companion object {
        @JvmStatic
        fun repositories() = listOf(
            Arguments.of(CartRedisRepository()),
        )
    }

    private fun getItemsFromJson(json: String): ArrayList<CartItem> {
        val list: List<CartItem> = Klaxon().parseArray(json)!!
        return ArrayList(list)
    }

    private fun initiateDB(cartRepository: CartRepository) {
        Jedis().flushAll()
        cartRepository.addOrUpdate(userId, p1.id, c1json)
        cartRepository.addOrUpdate(userId, p2.id, c2json)
        cartRepository.addOrUpdate(userId, p3.id, c3json)
    }

    // tests
    @ParameterizedTest
    @MethodSource("repositories")
    fun addsItemsCorrectly(cartRepository: CartRepository) {
        Jedis().flushAll()

        assertEquals(true, cartRepository.addOrUpdate(userId, p1.id, c1json))
        assertEquals(true, cartRepository.addOrUpdate(userId, p2.id, c2json))
        assertEquals(true, cartRepository.addOrUpdate(userId, p3.id, c3json))

        assertEquals(true, cartRepository.hasItem(userId, p1.id))
        assertEquals(true, cartRepository.hasItem(userId, p1.id))
        assertEquals(true, cartRepository.hasItem(userId, p1.id))
        assertEquals(false, cartRepository.hasItem(userId, np4.id))
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun getAllItemsCorrectly(cartRepository: CartRepository) {
        initiateDB(cartRepository)

        val items = getItemsFromJson(cartRepository.getAll(userId)!!)

        assert(items.contains(c1))
        assert(items.contains(c2))
        assert(items.contains(c3))
        assert(!items.contains(nc4))
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun getsCartItemById(cartRepository: CartRepository) {
        initiateDB(cartRepository)

        val product2 = klx.parse<CartItem>(cartRepository.getItem(userId, p2.id)!!)
        assertEquals(product2!!.product.id, p2.id)

        val product3 = klx.parse<CartItem>(cartRepository.getItem(userId, p3.id)!!)
        assertEquals(product3!!.product.id, p3.id)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun deletesItemCorrectly(cartRepository: CartRepository) {
        Jedis().flushAll()

        cartRepository.addOrUpdate(userId, p1.id, c1json)
        cartRepository.addOrUpdate(userId, p2.id, c2json)
        cartRepository.addOrUpdate(userId, p3.id, c3json)

        cartRepository.deleteItem(userId, p2.id)

        assert(cartRepository.hasItem(userId, p1.id))
        assert(cartRepository.hasItem(userId, p3.id))

        assert(!cartRepository.hasItem(userId, p2.id))

//        assertEquals(true, cartRepository.getAll())
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun deletesAllCorrectly(cartRepository: CartRepository) {
        Jedis().flushAll()

        cartRepository.addOrUpdate(userId, p1.id, c1json)
        cartRepository.addOrUpdate(userId, p2.id, c2json)
        cartRepository.addOrUpdate(userId, p3.id, c3json)

        assertNotNull(cartRepository.getAll(userId))
        cartRepository.deleteCart(userId)
        assertEquals(null, cartRepository.getAll(userId))
    }

}