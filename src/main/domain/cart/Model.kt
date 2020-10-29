package io.khashayar.domain.cart

import io.khashayar.domain.product.AlbumProduct

data class CartItem(val product: AlbumProduct, var quantity: Int) {
    fun incrementQuantity() {
        quantity++
    }

    fun decrementQuantity() {
        if (quantity > 1) {
            quantity--
        } else {
            print("quantity is one, instead of decrementing item should be deleted")
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is CartItem && this.hashCode() == other.hashCode()
    }

    override fun hashCode(): Int {
        var result = product.hashCode()
        result = 31 * result + quantity
        return result
    }
}