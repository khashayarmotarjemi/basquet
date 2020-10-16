package io.khashayar.domain.cart

import io.khashayar.domain.Price
import io.khashayar.domain.Product
import io.reactivex.rxjava3.core.Observable

class Cart {
    private val items: ArrayList<Product> = ArrayList()

    fun addItem(product: Product) {
        items.add(product)
    }

    fun removeItem(id: Int) {
        items.removeIf { product -> product.id == id }
    }

    fun total(): Price {
        return items.fold(initial = Price(0), operation = { prev, product -> prev.plus(product.price) })
    }

    fun count(): Int {
        return items.count()
    }
}