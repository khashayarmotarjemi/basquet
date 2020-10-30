package io.khashayar.data

import com.mongodb.BasicDBObject
import com.mongodb.DBCursor
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.TextSearchOptions
import org.bson.Document
import java.util.regex.Pattern


class ProductMongoRepository : ProductRepository {
    private val databaseName = "productsdb"
    private val collectionName = "products"

    private val mongo = MongoClient("localhost", 27017)
    private val database = mongo.getDatabase(databaseName)

    init {
//        getCollection().createIndex(Indexes.text("indexed_text"))
    }


    private fun getCollection(): MongoCollection<Document> {
        if (!database.listCollectionNames().contains(collectionName)) {
            database.createCollection(collectionName)
        }
        return database.getCollection(collectionName)
    }

    private fun parseDocument(productJson: String, productId: Long, indexedText: String) =
        Document(
            mapOf<String?, Any>(
                Pair("product_id", productId),
                Pair("product", productJson),
                Pair("indexed_text", indexedText)
            )
        )

    override fun getAll(start: Long, end: Long): String {
        return getCollection().find().map { it["product"].toString() }.toList().toString()
    }

    override fun itemCount(): Long {
        return getCollection().countDocuments()
    }

    override fun add(productJson: String, productId: Long, indexedText: String): Long {
        getCollection().insertOne(parseDocument(productJson, productId, indexedText))
        getCollection().createIndex(Indexes.text("indexed_text"))
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

    override fun search(query: String): String {/*
        val pattern = ".*$query.*"
//        val pattern = Pattern.compile(".*$query.*")

        return getCollection().find(Filters.regex("indexed_text", pattern)).map { it["product"].toString() }.toList()
            .toString()*/

        val ref: BasicDBObject = BasicDBObject()
        ref["indexed_text"] = Pattern.compile(".*$query.*", Pattern.CASE_INSENSITIVE)
        return getCollection().find(ref).map { it["product"].toString() }.toList().toString()
//        return getCollection().find(Filters.text(query)).map { it["product"].toString() }.toList().toString()


    }
}

