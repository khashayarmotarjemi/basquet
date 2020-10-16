package io.khashayar.data

import io.khashayar.domain.Product

class ProductsRepository {
    fun loadProducts(): LoadResult {
        return Loaded(ArrayList())
    }
}

open class LoadResult

class Loaded(private val items: ArrayList<Product>) : LoadResult()

class Error(private val reason: String = "s") : LoadResult()