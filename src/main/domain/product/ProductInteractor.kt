package io.khashayar.domain.product

import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import io.khashayar.data.ProductRepository


class ProductInteractor(private val productsRepository: ProductRepository) {
    private val klx: Klaxon = Klaxon()


    fun add(
        name: String,
        artist: String = "",
        year: String = "",
        price: Int,
        description: String = "",
        imageUrl: String = ""
    ) {
        val itemCount = productsRepository.itemCount()
        val id = itemCount + 1
        val product = AlbumProduct(
            id,
            name,
            Price(price, "$"),
            description = description,
            imageUrl = imageUrl,
            artistName = artist,
            releaseDate = year
        )
        productsRepository.add(klx.toJsonString(product), product.id, "$name $artist")
    }

    fun getAllJson(): String {
        return productsRepository.getAll()
    }

    fun getJsonById(productId: Long): String? {
        val json = productsRepository.getById(productId)
        return if (json == "") {
            null
        } else {
            json
        }
    }

    fun getById(productId: Long): AlbumProduct? {
        val productJson = productsRepository.getById(productId)
        if (productJson != "") {
            try {
                val product = klx.parse<AlbumProduct>(productJson)
                if (product != null) {
                    return product
                }
            } catch (ke: KlaxonException) {
                print(ke)
                print(ke.stackTrace)
            }
        }
        return null
    }

    fun getAll(): List<AlbumProduct> {
        return try {
            klx.parseArray(getAllJson()) ?: ArrayList()
        } catch (ke: KlaxonException) {
            print(ke)
            print(ke.stackTrace)
            ArrayList()
        }
    }

    fun deleteAll() {
        productsRepository.deleteAll()
    }

    fun search(query: String): String {
        return if (query == "") {
            "[]"
        } else {
            productsRepository.search(query)
        }
    }
}


