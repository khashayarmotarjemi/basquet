package io.khashayar.data

import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import org.bson.*


class CartMongoRepository : CartRepository {
    private val databaseName = "cartdb"
    private val mongo = MongoClient("localhost", 27017)
    private val database = mongo.getDatabase(databaseName)


    private fun getCollection(userId: Int): MongoCollection<Document> {
        return database.getCollection(userId.toString())
    }

    private fun getDocument(itemJson: String, productId: Long) =
        Document(
            mapOf<String?, Any>(
                Pair("product_id", productId),
                Pair("cart_item", itemJson)
            )
        )


    override fun getItem(userId: Int, productId: Long): String? {
        val item = getCollection(userId).find(Filters.eq("product_id", productId))
        return if (item.count() > 0) {
            item.first()["cart_item"].toString()
        } else {
            null
        }
    }


    override fun getAll(userId: Int): String? {
        val items = getCollection(userId).find().map { it["cart_item"].toString() }
        return if (items.toList().isNotEmpty()) {
            items.toList().toString()
        } else null

    }

    override fun addOrUpdate(userId: Int, productId: Long, cartItemJson: String): Boolean {
        getCollection(userId).replaceOne(
            Filters.eq("product_id", productId), getDocument(cartItemJson, productId),
            ReplaceOptions().upsert(true)
        )
        return true
    }


    override fun deleteCart(userId: Int): Boolean {
        getCollection(userId).drop()
        return true
    }

    override fun deleteItem(userId: Int, productId: Long): Boolean {
        getCollection(userId).deleteOne(Filters.eq("product_id", productId))
        return true
    }

    override fun hasCart(userId: Int): Boolean {
        return database.listCollectionNames().contains(userId.toString())
    }

    override fun hasItem(userId: Int, productId: Long): Boolean {
        return getCollection(userId).find(Filters.eq("product_id", productId)).count() > 0
    }

    override fun testClearAll() {
        mongo.dropDatabase(databaseName)
    }
}