package io.khashayar.data

interface ProductRepository {

    fun getAll(start: Long = 0, end: Long = 100): String

    fun itemCount(): Long

    fun add(productJson: String): Long

    fun deleteAll()

    fun getById(productId: Long): String

    fun addAll(products: List<String>) {
        products.map { json -> add(json) }
    }
}