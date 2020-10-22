package io.khashayar.test

import com.beust.klaxon.Klaxon
import io.khashayar.data.ProductRepository
import io.khashayar.domain.product.Price
import io.khashayar.domain.product.Product
import io.khashayar.data.ProductsRedisRepository
import io.khashayar.domain.product.ProductInteractor
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import redis.clients.jedis.Jedis

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ProductRepositoryTests {
    // init stuff
    private val klx = Klaxon()

    private val p1 = Product(1, "first product #1", Price(100, "$"))
    private val p2 = Product(2, "second product #2", Price(200, "$"))
    private val p3 = Product(3, "third product #3", Price(300, "$"))

    private val p1json = klx.toJsonString(p1)
    private val p2json = klx.toJsonString(p2)
    private val p3json = klx.toJsonString(p3)


    companion object {
        @JvmStatic
        fun repositories() = listOf(
            Arguments.of(ProductsRedisRepository()),
//            Arguments.of(ProductsRedisRepository()),
        )
    }


    private fun getItemsFromJson(json: String): ArrayList<Product> {
        val list: List<Product> = Klaxon().parseArray(json)!!
        return ArrayList(list)
    }

    // tests
    @ParameterizedTest
    @MethodSource("repositories")
    fun addsItemsCorrectly(productRepo: ProductRepository) {
        Jedis().flushAll()

//        print(p1json)

        assertEquals(productRepo.add(p1json), p1.id)
        assertEquals(productRepo.add(p2json), p2.id)
        assertEquals(productRepo.add(p3json), p3.id)

        val loaded: ArrayList<Product> = getItemsFromJson(productRepo.getAll())

        assertEquals(loaded.count(), 3)

        assert(loaded.contains(p1))
        assert(loaded.contains(p3))
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun getsProductById(productRepo: ProductRepository) {
        Jedis().flushAll()

        productRepo.add(p1json)
        productRepo.add(p2json)
        productRepo.add(p3json)

        val product2 = klx.parse<Product>(productRepo.getById(p2.id))
        assertEquals(product2!!.id, p2.id)

        val product3 = klx.parse<Product>(productRepo.getById(p3.id))
        assertEquals(product3!!.id, p3.id)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun deletesAllCorrectly(productRepo: ProductRepository) {
        Jedis().flushAll()

        productRepo.add(p1json)
        productRepo.add(p2json)

        productRepo.deleteAll()

        assertEquals("[]", productRepo.getAll())
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun getsLastIndexCorrectly(productRepo: ProductRepository) {
        Jedis().flushAll()

        productRepo.add(p1json)
        productRepo.add(p2json)
        productRepo.add(p3json)


        assertEquals(p3json, productRepo.getById(productRepo.itemCount()))
    }
}

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ProductInteractorTests {

    // init stuff
    private val p1 = Product(1, "first product #1", Price(100, "$"))
    private val p2 = Product(2, "second product #2", Price(200, "$"))
    private val p3 = Product(3, "third product #3", Price(300, "$"))

    private val repoDatabase = ArrayList<Product>()
    private val testItems = ArrayList<Product>()

    private val productRepo: ProductRepository = mockk()

    init {
        testItems.add(p1)
        testItems.add(p2)
        testItems.add(p3)

        every { productRepo.itemCount() } answers {
            when {
                repoDatabase.count() == 0 -> 0
                repoDatabase.count() == 1 -> 1
                repoDatabase.count() == 2 -> 2
                else -> -1
            }
        }

        every { productRepo.getAll(any(), any()) } answers {
            when {
                repoDatabase.count() == 0 -> Klaxon().toJsonString(repoDatabase)
                repoDatabase.count() == 1 -> Klaxon().toJsonString(repoDatabase)
                repoDatabase.count() == 2 -> Klaxon().toJsonString(repoDatabase)
                else -> Klaxon().toJsonString(repoDatabase)
            }
        }

        testItems.forEach { product ->
            val productJson = Klaxon().toJsonString(product)

            every { productRepo.getById(product.id) } returns productJson

            every { productRepo.add(productJson) } answers {
                repoDatabase.add(product)
                product.id
            }
        }

        every { productRepo.deleteAll() } answers { repoDatabase.clear() }
    }

    private fun getNewInteractor(): ProductInteractor {
        return ProductInteractor(productRepo)
    }

    // tests
    @Test
    fun addsItemsCorrectly() {
        val productInter = getNewInteractor()

        testItems.map { item -> productInter.add(item.name, item.price.amount, "") }

        assertEquals(productInter.getAll().count(), testItems.count())
    }

    @Test
    fun getsAllProducts() {
        val productInter = getNewInteractor()

        testItems.map { item -> productInter.add(item.name, item.price.amount, "") }

        assertEquals(testItems.size, productInter.getAll().size)
        assertEquals(testItems[1], productInter.getAll()[1])
    }

    @Test
    fun getsOneProductCorrectly() {
        val productInter = getNewInteractor()

        testItems.map { item -> productInter.add(item.name, item.price.amount, "") }

        assertEquals(p2, productInter.getById(p2.id))
    }
}