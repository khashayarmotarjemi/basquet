package io.khashayar.data

import redis.clients.jedis.Jedis

class ProductRedisRepository {
    private val jedis: Jedis = Jedis()

    private val key = "products"

    fun getAll(): String {
        val start: Long = 0
        val end: Long = 100

        return jedis.lrange(key, start, end).toString()
    }

    fun add(productJson: String, productId: Long) {
        jedis.lpush(key, productJson)
    }

    fun getById(productId: Long): String {
        return jedis.lindex(key, productId)
    }

    fun deleteAll() {
        jedis.del(key)
    }
}