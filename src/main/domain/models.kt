package io.khashayar.domain

class Product(val id: Int, val name: String, val price: Price) {

}

class Price(val amount: Int) {
    fun plus(price: Price): Price {
        return Price(amount + price.amount)
    }
}

