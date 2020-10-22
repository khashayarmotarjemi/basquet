package io.khashayar.domain.cart

import com.beust.klaxon.Klaxon
import io.khashayar.data.CartRedisRepository
import io.khashayar.domain.product.Price
import io.khashayar.domain.product.ProductInteractor

class CartInteractor(
    private val userId: Int,
    private val cartRepository: CartRedisRepository,
    private val productInteractor: ProductInteractor
) {

    fun itemsJson(): String {
        return cartRepository.getCartJson(userId) ?: ""
    }

    fun addItem(productId: Long): Boolean {
        val product = productInteractor.getById(productId)
        if (product != null && product.id == productId) {
            cartRepository.add(userId, product)
            return true
        }
        return false
    }

    fun deleteCart() {
        cartRepository.deleteCart(userId)
    }

    fun deleteItem(productId: Long) {
        cartRepository.removeItem(userId, productId)
    }

    fun decrementQuantity(productId: Long) {
        cartRepository.decrementOne(userId, productId)
    }

    fun totalPrice(): Price {
        return cartRepository.total(userId)
    }
}

open class CartData

class CartLoaded(private val items: List<CartItem>) : CartData() {

}

class CartLoadingFailed : CartData() {

}