package io.khashayar.domain

import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException

class Product(val id: Int, val name: String, val price: Price) {
}

class Price(val amount: Int, val currency: String) {
    fun plus(price: Price): Price {
        return Price(amount + price.amount, "$")
    }
}

