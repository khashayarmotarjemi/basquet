package io.khashayar.data

import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import org.bson.Document

class ProductMongoRepository : ProductRepository {
    private val databaseName = "productsdb"
    private val collectionName = "products"

    private val mongo = MongoClient("localhost", 27017)
    private val database = mongo.getDatabase(databaseName)


    private fun getCollection(): MongoCollection<Document> {
        if (!database.listCollectionNames().contains(collectionName)) {
            database.createCollection(collectionName)
        }
        return database.getCollection(collectionName)
    }

    private fun parseDocument(productJson: String, productId: Long) =
        Document(
            mapOf<String?, Any>(
                Pair("product_id", productId),
                Pair("product", productJson)
            )
        )


    override fun getAll(start: Long, end: Long): String {
        return getCollection().find().map { it["product"].toString() }.toList().toString()

    }

    override fun itemCount(): Long {
        return getCollection().countDocuments()
    }

    override fun add(productJson: String, productId: Long): Long {
        getCollection().insertOne(parseDocument(productJson, productId))
        return productId
    }

    override fun deleteAll() {
        getCollection().drop()
    }

    override fun getById(productId: Long): String {
        val item = getCollection().find(Filters.eq("product_id", productId))
        return if (item.count() > 0) {
            item.first()["product"].toString()
        } else {
            ""
        }
    }

    override fun testDeleteEverything() {
        database.drop()
    }

}
