package io.khashayar.domain.product

import com.beust.klaxon.Klaxon

class Product(val id: Int, val name: String, val price: Price) {
    fun toJson(): String {
        return Klaxon().toJsonString(this)
    }

    override fun equals(other: Any?): Boolean {
        return other is Product && other.id == id
    }
}

class Price(val amount: Int, val currency: String) {
    fun plus(price: Price): Price {
        return Price(amount + price.amount, "$")
    }

    fun plus(addAmount: Int): Price {
        return Price(amount + addAmount, "$")
    }
}