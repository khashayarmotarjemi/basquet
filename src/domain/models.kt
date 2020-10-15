package io.khashayar.domain

class Product(val id: Int, val name: String, val price: Price) {

}

class Price(val amount: Int) {
    fun plus(price: Price): Price {
        return Price(amount + price.amount)
    }
}

class Cart(private val items: ArrayList<Product> = ArrayList()) {
    fun addItem(product: Product) {
        items.add(product)
    }

    fun total(): Price {
        return items.fold(initial = Price(0), operation = { acc, product -> acc.plus(product.price) })
    }
}

