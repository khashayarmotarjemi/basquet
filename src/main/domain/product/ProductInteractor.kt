/*
package io.khashayar.domain.product

import io.khashayar.data.CartRedisRepository
import io.khashayar.data.ProductsRepository
import io.khashayar.domain.product.Price
import io.khashayar.domain.product.Product

class ProductInteractor(private val productsRepository: ProductsRepository) {

//    init {
//        val p1 = Product(1, "first product #1", Price(100, "$"))
//        val p2 = Product(2, "first product #2", Price(200, "$"))
//        cartRepository.add(userId, p1)
//        cartRepository.add(userId, p2)
//    }


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

}*/
