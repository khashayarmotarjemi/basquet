package io.khashayar.domain.cart

import io.khashayar.domain.product.Product

data class CartItem(val product: Product, var quantity: Int) {
    fun incrementQuantity() {
        quantity++
    }

    fun decrementQuantity() {
        quantity--
    }
}