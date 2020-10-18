package io.khashayar.test

import com.beust.klaxon.Klaxon
import io.khashayar.domain.cart.CartItem
import io.khashayar.domain.product.Price
import io.khashayar.domain.product.Product
import io.khashayar.data.ProductRedisRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import redis.clients.jedis.Jedis

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductRepositoryTests {
    // init stuff
    private val klx = Klaxon()

    private val p1 = Product(1, "first product #1", Price(100, "$"))
    private val p2 = Product(2, "first product #2", Price(200, "$"))

    private val p1json = klx.toJsonString(p1)
    private val p2json = klx.toJsonString(p2)


    private fun getNewRepo(): ProductRedisRepository {
        return ProductRedisRepository()
    }

    private fun getItemsFromJson(json: String): ArrayList<Product> {
        val list: List<Product> = Klaxon().parseArray(json)!!
        return ArrayList(list)
    }

    // tests
    @Test
    fun addsItemsCorrectly() {
        Jedis().flushAll()

        val productRepo = getNewRepo()

        productRepo.add(p1json, p1.id.toLong())
        productRepo.add(p2json, p2.id.toLong())

        val loaded: ArrayList<Product> = getItemsFromJson(productRepo.getAll())

        assertEquals(loaded.count(), 2)

        assert(loaded.contains(p1))
        assert(loaded.contains(p2))
    }

    @Test
    fun getsProductById() {
        Jedis().flushAll()

        val productRepo = getNewRepo()

        productRepo.add(p1json, p1.id.toLong())
        productRepo.add(p2json, p2.id.toLong())

        val product = klx.parse<Product>(productRepo.getById(p1.id.toLong()))

        assertEquals(product, p1)
    }

    @Test
    fun deletesAllCorrectly() {
        Jedis().flushAll()

        val productRepo = getNewRepo()

        productRepo.add(p1json, p1.id.toLong())
        productRepo.add(p2json, p2.id.toLong())

        productRepo.deleteAll()

        assertEquals("[]", productRepo.getAll())
    }
}

