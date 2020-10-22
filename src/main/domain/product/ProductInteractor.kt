package io.khashayar.domain.product

import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import io.khashayar.data.ProductRepository


class ProductInteractor(private val productsRepository: ProductRepository) {
    private val klx: Klaxon = Klaxon()


    fun add(name: String, price: Int, description: String, imageId: String = "") {
        val itemCount = productsRepository.itemCount()
        val id = itemCount + 1
        val product = Product(id, name, Price(price, "$"), description = description, imageId = imageId)
        productsRepository.add(klx.toJsonString(product))
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

    fun getById(productId: Long): Product? {
        val productJson = productsRepository.getById(productId)
        if (productJson != "") {
            try {
                val product = klx.parse<Product>(productJson)
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

    fun getAll(): List<Product> {
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
}


