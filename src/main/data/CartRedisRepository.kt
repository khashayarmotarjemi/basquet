package io.khashayar.data

import com.beust.klaxon.KlaxonException
import redis.clients.jedis.Jedis

// TODO: move json parsing and conversion to the interactor

class CartRedisRepository : CartRepository {
    private val jedis: Jedis = Jedis()

    override fun hasCart(userId: Int): Boolean {
        return try {
            jedis.exists(userId.toString())
        } catch (ke: KlaxonException) {
            print(ke)
            print(ke.stackTrace)
            false
        }
    }

    override fun hasItem(userId: Int, productId: Long): Boolean {
        return try {
            jedis.hexists(userId.toString(), productId.toString())
        } catch (ke: KlaxonException) {
            print(ke)
            print(ke.stackTrace)
            false
        }
    }


    override fun getItem(userId: Int, productId: Long): String? {
        val itemJson: String
        try {
            if(hasItem(userId,productId)) {
                itemJson = jedis.hget(userId.toString(), productId.toString())
            } else {
                return null
            }
        } catch (ke: KlaxonException) {
            print(ke)
            print(ke.stackTrace)
            return null
        }
        return itemJson
    }

    override fun getAll(userId: Int): String? {
        return if (hasCart(userId)) {
            val cartData = jedis.hgetAll(userId.toString())
            return cartData.values.toString()
        } else {
            null
        }
    }

    override fun addOrUpdate(userId: Int, productId: Long, cartItemJson: String): Boolean {
        try {
            jedis.hset(userId.toString(), productId.toString(), cartItemJson)
        } catch (ke: KlaxonException) {
            print(ke)
            print(ke.stackTrace)
            return false
        }
        return true
    }

    override fun deleteCart(userId: Int): Boolean {
        if (hasCart(userId)) {
            try {
                jedis.del(userId.toString())
            } catch (ke: KlaxonException) {
                print(ke)
                print(ke.stackTrace)
                return false
            }
            return true
        } else {
            print("Database doesn't have a cart for user with id: $userId")
            return false
        }
    }

    override fun deleteItem(userId: Int, productId: Long): Boolean {
        if (hasCart(userId)) {
            try {
                jedis.hdel(userId.toString(), productId.toString())
            } catch (ke: KlaxonException) {
                print(ke)
                print(ke.stackTrace)
                return false
            }
            return true
        } else {
            print("Database doesn't have a cart for user with id: $userId")
            return false
        }
    }

    override fun testClearAll() {
        jedis.flushAll()
    }
}