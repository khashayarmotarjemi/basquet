package io.khashayar.data

import io.khashayar.domain.Product

class CartRepository {
    fun loadProducts(): LoadResult {
        return Loaded(ArrayList())
    }
}