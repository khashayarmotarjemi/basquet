package io.khashayar.test

import com.beust.klaxon.Klaxon
import io.khashayar.data.CartRedisRepository
import io.khashayar.data.CartRepository
import io.khashayar.domain.cart.CartInteractor
import io.khashayar.domain.cart.CartItem
import io.khashayar.domain.product.ProductInteractor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import redis.clients.jedis.Jedis
import kotlin.test.assertFalse
import kotlin.test.assertNotNull


class CartRepoMocks(private val cartRepository: CartRepository) {
    private val helper = Helper()

    fun getEmptyRepo(): CartRepository {
        Jedis().flushAll()
        return cartRepository
    }

    fun getPopulatedRepo(): CartRepository {
        Jedis().flushAll()
        cartRepository.addOrUpdate(helper.userIdWithCart, helper.p1.id, helper.c1json)
        cartRepository.addOrUpdate(helper.userIdWithCart, helper.p2.id, helper.c2json)
        cartRepository.addOrUpdate(helper.userIdWithCart, helper.p3.id, helper.c3json)

        return cartRepository

    }
}



@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class CartRepositoryTests {
    // init stuff
    private val klx = Klaxon()
    private val helper = Helper()

    companion object {
        private val mocks = CartRepoMocks(CartRedisRepository())

        @JvmStatic
        fun mocks() = listOf(
            Arguments.of(mocks),
        )
    }

    private fun getItemsFromJson(json: String): ArrayList<CartItem> {
        val list: List<CartItem> = Klaxon().parseArray(json)!!
        return ArrayList(list)
    }

    // tests
    @ParameterizedTest
    @MethodSource("mocks")
    fun addsAndGetsAllCorrectly(mocks: CartRepoMocks) {
        val loaded: ArrayList<CartItem> =
            getItemsFromJson(mocks.getPopulatedRepo().getAll(helper.userIdWithCart) ?: "")

        assertEquals(loaded.count(), 3)

        assert(loaded.contains(helper.c1))
        assert(loaded.contains(helper.c2))
        assert(loaded.contains(helper.c3))
        assert(!loaded.contains(helper.nc4))
    }

    @ParameterizedTest
    @MethodSource("mocks")
    fun getsCartItemById(mocks: CartRepoMocks) {
        val cartRepository = mocks.getPopulatedRepo()

        val cartItem2 = klx.parse<CartItem>(cartRepository.getItem(helper.userIdWithCart, helper.c2.product.id)!!)
        assertEquals(cartItem2!!.product.id, helper.c2.product.id)

        val cartItem3 = klx.parse<CartItem>(cartRepository.getItem(helper.userIdWithCart, helper.c3.product.id)!!)
        assertEquals(cartItem3!!.product.id, helper.c3.product.id)
    }

    @ParameterizedTest
    @MethodSource("mocks")
    fun deletesItemCorrectly(mocks: CartRepoMocks) {

        val cartRepository = mocks.getPopulatedRepo()

        cartRepository.deleteItem(helper.userIdWithCart, helper.c2.product.id)

        assert(cartRepository.hasItem(helper.userIdWithCart, helper.p1.id))
        assert(cartRepository.hasItem(helper.userIdWithCart, helper.p3.id))

        assertFalse(cartRepository.hasItem(helper.userIdWithCart, helper.c2.product.id))
        assertFalse(cartRepository.hasItem(helper.userIdWithCart, helper.nc4.product.id))
    }

    @ParameterizedTest
    @MethodSource("mocks")
    fun deletesAllCorrectly(mocks: CartRepoMocks) {
//        Jedis().flushAll()
        val cartRepository = mocks.getPopulatedRepo()

        assertNotNull(cartRepository.getAll(helper.userIdWithCart))
        cartRepository.deleteCart(helper.userIdWithCart)
        assertEquals(null, cartRepository.getAll(helper.userIdWithCart))
    }

}


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class CartInteractorTests {
    //    private val mocks = ProductInterMocks()
    private val helper = Helper()

    private fun getNewInteractor(): CartInteractor {
        return CartInteractor(
            helper.userIdWithCart,
            helper.getMockCartRepo(),
            helper.getPopulatedMockProductInteractor()
        )
    }

    @Test
    fun addsAndGetsItemsCorrectly() {
        val cartInteractor = getNewInteractor()

        helper.testCartItems.map { item -> cartInteractor.addItem(item.product.id) }

        assertEquals(3, cartInteractor.items().count())

        cartInteractor.addItem(helper.p1.id)

        assertEquals(3, cartInteractor.items().count())

        assertEquals(2, cartInteractor.items().find { it.product.id == helper.p1.id }!!.quantity)
        assertEquals(1, cartInteractor.items().find { it.product.id == helper.p2.id }!!.quantity)
        assertEquals(1, cartInteractor.items().find { it.product.id == helper.p3.id }!!.quantity)

        assertFalse(cartInteractor.items().contains(helper.nc4))

    }

    @Test
    fun getsItemsJsonCorrectly() {
        val cartInteractor = getNewInteractor()

        helper.testCartItems.map { item -> cartInteractor.addItem(item.product.id) }

        assertEquals(Klaxon().toJsonString(helper.testCartItems), cartInteractor.itemsJson())
    }

    @Test
    fun deletesItemCorrectly() {
        val cartInteractor = getNewInteractor()

        helper.testCartItems.map { item -> cartInteractor.addItem(item.product.id) }

        cartInteractor.deleteItem(helper.p2.id)

        assert(cartInteractor.items().contains(helper.c1))
        assert(cartInteractor.items().contains(helper.c3))
        assertFalse(cartInteractor.items().contains(helper.c2))
    }

    @Test
    fun decrementsQuantityCorrectly() {
        val cartInteractor = getNewInteractor()

        helper.testCartItems.map { item -> cartInteractor.addItem(item.product.id) }
        cartInteractor.addItem(helper.p1.id)

        assertEquals(2, cartInteractor.items().find { it.product.id == helper.p1.id }!!.quantity)

        cartInteractor.decrementQuantity(helper.p1.id)

        assertEquals(1, cartInteractor.items().find { it.product.id == helper.p1.id }!!.quantity)
    }
}