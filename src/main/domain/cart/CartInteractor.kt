package io.khashayar.domain.cart

import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import io.khashayar.data.CartRepository
import io.khashayar.domain.product.Price
import io.khashayar.domain.product.ProductInteractor

class CartInteractor(
    private val userId: Int,
    private val cartRepository: CartRepository,
    private val productInteractor: ProductInteractor
) {

    private val klx: Klaxon = Klaxon()

    fun itemsJson(): String {
        return cartRepository.getAll(userId) ?: "[]"
    }

    fun items(): List<CartItem> {
        val itemsList: List<CartItem>?
        try {
            itemsList = klx.parseArray(itemsJson())
        } catch (ke: KlaxonException) {
            print(ke)
            print(ke.stackTrace)
            return ArrayList()
        }

        return itemsList ?: ArrayList()
    }

    private fun getItem(productId: Long): CartItem? {
        return items().find { it.product.id == productId }
    }


    // for now this function parses the item then increments quantity
    // and converts it back to json string, it can be made more efficient
    // by directly manipulating the string (I guess).
    @Throws(KlaxonException::class)
    private fun getIncrementedJson(initialJson: String): String {
        val item = klx.parse<CartItem>(initialJson)

        if (item == null) {
            throw KlaxonException("could not parse json: $initialJson")
        } else {
            item.incrementQuantity()
            return klx.toJsonString(item)
        }
    }


    fun addItem(productId: Long): Boolean {
        val itemJson = cartRepository.getItem(userId, productId)
        if (itemJson != null) {
            return try {
                val incJson = getIncrementedJson(itemJson)
                cartRepository.addOrUpdate(userId, productId, incJson)
                true
            } catch (ke: KlaxonException) {
                print(ke)
                print(ke.stackTrace)
                false
            }
        } else {
            val product = productInteractor.getById(productId)
            return if (product != null) {
                val cartItemJson = klx.toJsonString(CartItem(product, 1))
                cartRepository.addOrUpdate(userId, productId, cartItemJson)
                true
            } else {
                false
            }
        }
    }

    fun deleteCart() {
        cartRepository.deleteCart(userId)
    }

    fun deleteItem(productId: Long) {
        cartRepository.deleteItem(userId, productId)
    }

    fun decrementQuantity(productId: Long) {
        getItem(productId)?.decrementQuantity()
    }

    fun totalPrice(): Price {
        return items().fold(Price(0, "$"),
            { sum, cartItem ->
                sum.plus(cartItem.product.price)
            })
    }
}
