package io.khashayar.domain.cart

import com.beust.klaxon.Klaxon
import io.khashayar.domain.Price
import io.khashayar.domain.Product
import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.JedisException

class Cart(private val userId: Int) {
    private val jedis: Jedis = Jedis()
    private val klx: Klaxon = Klaxon()

    private fun updateData(items: List<CartItem>) {
        jedis.hset("cart", userId.toString(), klx.toJsonString(items))
    }

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
                    updateData(newItems)

                } else {
                    oldItems.add(CartItem(product, 1))
                    updateData(oldItems)
                }
            } else {
                updateData(arrayListOf(CartItem(product, 1)))
            }
        } catch (je: JedisException) {
            print(je)
            print(je.stackTrace)
        }
    }

    fun removeItem(id: Int) {
        val items = getCart()
        if (items.map { item -> item.product.id }.contains(id)) {
            items.removeIf { item -> item.product.id == id }
            updateData(items)
        } else {
            print("Item to delete with id $id doesn't exist")
        }
    }

    fun decrementOne(id: Int) {
        val items = getCart()
        if (items.map { item -> item.product.id }.contains(id)) {
            if (items.find { item -> item.product.id == id }?.quantity == 1) {
                items.removeIf { item -> item.product.id == id }
                updateData(items)
            } else {
                val newItems: List<CartItem> = items.map { cartItem ->
                    if (cartItem.product.id == id) {
                        cartItem.decrementQuantity()
                    }
                    cartItem
                }
                updateData(newItems)
            }
        } else {
            print("Item to delete with id $id doesn't exist")
        }
    }

    fun total(): Price {
        return getCart().fold(
            initial = Price(0, "$"),
            operation = { prev, item -> prev.plus(item.product.price.amount * item.quantity) })
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

    fun decrementQuantity() {
        quantity--
    }
}