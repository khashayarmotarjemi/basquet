package io.khashayar.test

import com.beust.klaxon.Klaxon
import io.khashayar.data.CartRepository
import io.khashayar.data.ProductMongoRepository
import io.khashayar.data.ProductRepository
import io.khashayar.domain.product.Price
import io.khashayar.domain.product.AlbumProduct
import io.khashayar.data.ProductsRedisRepository
import io.khashayar.domain.cart.CartItem
import io.khashayar.domain.product.ProductInteractor
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import redis.clients.jedis.Jedis

class Helper {
    private val klx = Klaxon()

    private val mockProductRepo: ProductRepository = mockk()
    private val mockCartRepo: CartRepository = mockk()
    private val mockProductInter: ProductInteractor = mockk()

    val userIdWithCart = 999

    val p1 = AlbumProduct(1, "first product #1", Price(100, "$"))
    val p2 = AlbumProduct(2, "second product #2", Price(200, "$"))
    val p3 = AlbumProduct(3, "third product #3", Price(300, "$"))
    val np4 = AlbumProduct(4, "third product #4", Price(400, "$"))

    val c1 = CartItem(p1, 1)
    val c2 = CartItem(p2, 1)
    val c3 = CartItem(p3, 1)
    val nc4 = CartItem(np4, 1)

    val p1json = klx.toJsonString(p1)
    val p2json = klx.toJsonString(p2)
    val p3json = klx.toJsonString(p3)
    val np4json = klx.toJsonString(np4)

    val c1json = klx.toJsonString(c1)
    val c2json = klx.toJsonString(c2)
    val c3json = klx.toJsonString(c3)
    val nc4json = klx.toJsonString(nc4)

    // these lists have all the products in them
    val testProducts = ArrayList<AlbumProduct>()
    val testCartItems = ArrayList<CartItem>()

    // these list get interactively populated through the tests
    private val productDB = ArrayList<AlbumProduct>()
    private val cartItemDB = ArrayList<CartItem>()

    init {
        testProducts.add(p1)
        testProducts.add(p2)
        testProducts.add(p3)

        testCartItems.add(c1)
        testCartItems.add(c2)
        testCartItems.add(c3)

        Jedis().flushAll()
        initiateProductsRepoMock()
        initiateProductsInterMock()
        initiateCartRepoMock()
    }

    private fun initiateProductsRepoMock() {

        every { mockProductRepo.itemCount() } answers {
            when {
                productDB.count() == 0 -> 0
                productDB.count() == 1 -> 1
                productDB.count() == 2 -> 2
                else -> -1
            }
        }

        every { mockProductRepo.getAll(any(), any()) } answers {
            when {
                productDB.count() == 0 -> Klaxon().toJsonString(productDB)
                productDB.count() == 1 -> Klaxon().toJsonString(productDB)
                productDB.count() == 2 -> Klaxon().toJsonString(productDB)
                else -> Klaxon().toJsonString(productDB)
            }
        }

        testProducts.forEach { product ->
            val productJson = Klaxon().toJsonString(product)

            every { mockProductRepo.getById(product.id) } returns productJson

            every { mockProductRepo.add(productJson, product.id,"${product.name} ${product.artistName}") } answers {
                productDB.add(product)
                product.id
            }
        }

        every { mockProductRepo.deleteAll() } answers { productDB.clear() }
    }

    private fun initiateCartRepoMock() {
        every { mockCartRepo.getAll(userIdWithCart) } answers {
            when {
                cartItemDB.count() == 0 -> Klaxon().toJsonString(cartItemDB)
                cartItemDB.count() == 1 -> Klaxon().toJsonString(cartItemDB)
                cartItemDB.count() == 2 -> Klaxon().toJsonString(cartItemDB)
                else -> Klaxon().toJsonString(cartItemDB)
            }
        }

        testCartItems.forEach { cartItem ->

            val itemJson = klx.toJsonString(cartItem)

            val incrementedItem = klx.parse<CartItem>(itemJson)!!
            incrementedItem.incrementQuantity()
            val incrementedItemJson = klx.toJsonString(incrementedItem)

            every { mockCartRepo.getItem(userIdWithCart, cartItem.product.id) } answers {
                val itemInDB = cartItemDB.find { item -> item.product.id == cartItem.product.id }
                if (itemInDB == null) {
                    null
                } else {
                    Klaxon().toJsonString(itemInDB)
                }
            }

            every { mockCartRepo.addOrUpdate(userIdWithCart, cartItem.product.id, itemJson) } answers {
                if (cartItemDB.find { it.product.id == cartItem.product.id } == null) {
                    cartItemDB.add(cartItem)
                } else {
                    cartItemDB.removeIf { it.product.id == cartItem.product.id }
                    cartItemDB.add(cartItem)
                }
            }

            every { mockCartRepo.addOrUpdate(userIdWithCart, cartItem.product.id, incrementedItemJson) } answers {
                cartItemDB.remove(cartItem)
                cartItemDB.add(incrementedItem)
            }


            every { mockCartRepo.deleteItem(userIdWithCart, cartItem.product.id) } answers {
                cartItemDB.remove(cartItem)
            }

            every { mockCartRepo.hasItem(userIdWithCart, cartItem.product.id) } returns cartItemDB.contains(cartItem)
        }

        every { mockCartRepo.deleteCart(userIdWithCart) } answers {
            cartItemDB.clear()
            true
        }
    }

    private fun initiateProductsInterMock() {
        every { mockProductInter.getAll() } returns testProducts

        every { mockProductInter.getAllJson() } returns klx.toJsonString(testProducts)

        testProducts.forEach { testProduct ->
//            val productInDB = productDB.find { p -> p.id == testProduct.id }
//            val productJsonInDB = Klaxon().toJsonString(productInDB)

            val productJson = klx.toJsonString(testProduct)
            /* every {
                 mockProductInter.add(
                     testProduct.name,
                     testProduct.price.amount,
                     testProduct.description,
                     testProduct.imageId
                 )
             } answers {
                 productDB.add(testProduct)
             }*/

            every { mockProductInter.getById(testProduct.id) } returns testProduct

            every { mockProductInter.getJsonById(testProduct.id) } returns productJson
        }

        /*  every { mockProductInter.deleteAll() } answers
                  { productDB.clear() }*/
    }

    fun getEmptyMockProductRepo(): ProductRepository {
        return mockProductRepo
    }

    fun getPopulatedMockProductRepo(): ProductRepository {
        Jedis().flushAll()

        mockProductRepo.add(p1json, p1.id)
        mockProductRepo.add(p2json, p2.id)
        mockProductRepo.add(p3json, p3.id)
        return mockProductRepo
    }

    fun getMockCartRepo(): CartRepository {
        return mockCartRepo
    }

    fun getEmptyMockProductInteractor(): ProductInteractor {
        return mockProductInter
    }

    fun getPopulatedMockProductInteractor(): ProductInteractor {

        return mockProductInter
    }
}

class ProductRepoMocks(private val productRepo: ProductRepository) {
    private val helper = Helper()

    fun getEmptyRepo(): ProductRepository {
        productRepo.testDeleteEverything()
        return productRepo
    }

    fun getPopulatedRepo(): ProductRepository {
        productRepo.testDeleteEverything()

        productRepo.add(helper.p1json, helper.p1.id)
        productRepo.add(helper.p2json, helper.p2.id)
        productRepo.add(helper.p3json, helper.p3.id)
        return productRepo
    }
}

class ProductInterMocks {
    private val helper = Helper()

    fun getInteractor(): ProductInteractor {
        return ProductInteractor(helper.getEmptyMockProductRepo())
    }
}


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ProductRepositoryTests {
    // init stuff
    private val klx = Klaxon()
    private val helper = Helper()

    companion object {
        private val redis = ProductRepoMocks(ProductsRedisRepository())
        private val mongo = ProductRepoMocks(ProductMongoRepository())

        @JvmStatic
        fun mocks() = listOf(
            Arguments.of(redis),
            Arguments.of(mongo),
        )
    }

    private fun getItemsFromJson(json: String): ArrayList<AlbumProduct> {
        val list: List<AlbumProduct> = Klaxon().parseArray(json)!!
        return ArrayList(list)
    }

    // tests
    @ParameterizedTest
    @MethodSource("mocks")
    fun addsItemsCorrectly(mocks: ProductRepoMocks) {
        val loaded: ArrayList<AlbumProduct> = getItemsFromJson(mocks.getPopulatedRepo().getAll())

        assertEquals(loaded.count(), 3)

        assert(loaded.contains(helper.p1))
        assert(loaded.contains(helper.p3))
        assert(!loaded.contains(helper.np4))
    }

    @ParameterizedTest
    @MethodSource("mocks")
    fun getsProductById(mocks: ProductRepoMocks) {

        val repo = mocks.getPopulatedRepo()

        val product2 = klx.parse<AlbumProduct>(repo.getById(helper.p2.id))
        assertEquals(product2!!.id, helper.p2.id)

        val product3 = klx.parse<AlbumProduct>(repo.getById(helper.p3.id))
        assertEquals(product3!!.id, helper.p3.id)
    }

    @ParameterizedTest
    @MethodSource("mocks")
    fun deletesAllCorrectly(mocks: ProductRepoMocks) {
        val repo = mocks.getPopulatedRepo()

        repo.deleteAll()

        assertEquals("[]", repo.getAll())
    }

    @ParameterizedTest
    @MethodSource("mocks")
    fun getsLastIndexCorrectly(mocks: ProductRepoMocks) {
        val repo = mocks.getPopulatedRepo()

        assertEquals(helper.p3json, repo.getById(repo.itemCount()))
    }
}

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ProductInteractorTests {
    private val mocks = ProductInterMocks()
    private val helper = Helper()

    // tests
    @Test
    fun addsItemsCorrectly() {
        val productInter = mocks.getInteractor()

        helper.testProducts.map { item -> productInter.add(item.name, price = item.price.amount) }

        assertEquals(productInter.getAll().count(), helper.testProducts.count())
    }

    @Test
    fun getsAllProducts() {
        val productInter = mocks.getInteractor()


        helper.testProducts.map { item -> productInter.add(item.name, price = item.price.amount) }

        assertEquals(helper.testProducts.size, productInter.getAll().size)
        assertEquals(helper.testProducts[1], productInter.getAll()[1])
    }

    @Test
    fun getsOneProductCorrectly() {
        val productInter = mocks.getInteractor()


        helper.testProducts.map { item -> productInter.add(item.name, price = item.price.amount) }

        assertEquals(helper.p2, productInter.getById(helper.p2.id))
    }
}