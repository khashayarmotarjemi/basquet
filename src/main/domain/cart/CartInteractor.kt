package io.khashayar.domain.cart

import io.khashayar.data.CartRedisRepository
import io.khashayar.domain.product.Price
import io.khashayar.domain.product.Product

class CartInteractor(private val userId: Int, private val cartRepository: CartRedisRepository) {



    fun itemsJson(): String {
        return cartRepository.getCartJson(userId)
    }

    fun addItem(product: Product) {
        cartRepository.add(userId, product)
    }

    fun deleteCart() {
        cartRepository.deleteCart(userId)
    }

    fun deleteItem(productId: Int) {
        cartRepository.removeItem(userId, productId)
    }

    fun decrementQuantity(productId: Int) {
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