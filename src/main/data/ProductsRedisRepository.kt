package io.khashayar.data

import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.JedisException

class ProductsRedisRepository : ProductRepository {
    private val jedis: Jedis = Jedis()

    private val key = "products"


    override
    fun getAll(start: Long, end: Long): String {
        return jedis.lrange(key, start, end).toList().toString()
    }


    // returns the size of the
    override
    fun add(productJson: String, productId: Long): Long {
        return jedis.rpush(key, productJson)
    }

    override
    fun itemCount(): Long {
        return jedis.llen(key).toLong()
    }

    override
    fun getById(productId: Long): String {
        return try {
            jedis.lindex(key, productId - 1) ?: ""
        } catch (je: JedisException) {
            print(je)
            print(je.stackTrace)
            ""
        }
    }

    override fun testDeleteEverything() {
        jedis.flushAll()
    }

    override
    fun deleteAll() {
        jedis.del(key)
    }
}