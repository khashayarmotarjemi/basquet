package io.khashayar.domain.cart

import com.beust.klaxon.Klaxon
import io.khashayar.domain.Price
import io.khashayar.domain.Product
import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.JedisException

class Cart(private val userId: Int) {
    //    private val items: ArrayList<Product> = ArrayList()
    private val jedis: Jedis = Jedis()
    private val klx: Klaxon = Klaxon()

    fun getCart(): ArrayList<CartItem> {
        val productsList = jedis.hget("cart", userId.toString())
        return try {
            val list: List<CartItem> = klx.parseArray(productsList)!!
            ArrayList(list)
        } catch (e: Exception) {
            print(e)
            print(e.stackTrace)
            ArrayList()
        }
    }

    fun addItem(product: Product) {
/*
        val productJson = klx.toJsonString(product)
*/
        try {
            if (jedis.hexists("cart", userId.toString())) {
                val oldItems = getCart()

                if (oldItems.map { item -> item.product.id }.contains(product.id)) {
                    val newItems: List<CartItem> = oldItems.map { cartItem ->
                        if (cartItem.product.id == product.id) {
                            cartItem.incrementQuantity()
                        }
                        cartItem
                    }
                    jedis.hset("cart", userId.toString(), klx.toJsonString(newItems))

                } else {
                    oldItems.add(CartItem(product, 1))
                    jedis.hset("cart", userId.toString(), klx.toJsonString(oldItems))
                }
            } else {
                val itemsJson = klx.toJsonString(arrayListOf(CartItem(product, 1)))
                jedis.hset("cart", userId.toString(), itemsJson)
            }
        } catch (je: JedisException) {
            print(je)
            print(je.stackTrace)
        }
    }

    fun removeItem(id: Int) {
//        items.removeIf { product -> product.id == id }

    }

    fun total(): Price {
//        return items.fold(initial = Price(0), operation = { prev, product -> prev.plus(product.price) })
        return Price(1, "")
    }

    fun count(): Int {
        return 0
//        return items.count()
    }
}

open class CartData {

}

class CartLoaded(private val items: List<CartItem>) : CartData() {

}

class CartLoadingFailed : CartData() {

}

data class CartItem(val product: Product, var quantity: Int) {
    fun incrementQuantity() {
        quantity++
    }
}