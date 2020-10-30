package io.khashayar.data

interface ProductRepository {

    fun getAll(start: Long = 0, end: Long = 100): String

    fun itemCount(): Long

    fun add(productJson: String, productId: Long, indexedText: String = ""): Long

    fun deleteAll()

    fun getById(productId: Long): String

    fun testDeleteEverything()

    fun search(query: String) : String
}