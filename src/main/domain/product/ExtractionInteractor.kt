package io.khashayar.domain.product

import redis.clients.jedis.Jedis

class ExtractionInteractor(private val productInteractor: ProductInteractor) {

    fun populateDB() {
        Jedis().flushAll()

        productInteractor.add(
            "Tweez",
            12,
            imageId = "BRwqzx3GY",
            description = "Includes FREE MP3 version of this album. "
        )
        productInteractor.add(
            "London Calling",
            17,
            imageId = "qd9oT7EGe",
            description = "Best Sellers Rank: 341 in CDs & Vinyl (See Top 100 in CDs & Vinyl)"
        )
        productInteractor.add(
            "The Man Machine",
            24,
            imageId = "p30wgwqiU",
            description = "The seventh studio album by the German electronic band. Includes the singles 'Neon Lights', 'The Robots' and 'The Model'"
        )
        productInteractor.add("Cunning Stunts by Cows", 33, imageId = "bYHIZeeTJ", description = "")
        productInteractor.add("Entertainment", 15, imageId = "U79grxOUn", description = "")
        productInteractor.add("Bookends", 14, imageId = "s8Qu0k2nI", description = "")
        productInteractor.add("Bleach", 24, imageId = "Zil1YENhm", description = "")
        productInteractor.add("Physical Graffiti", 27, imageId = "2brObXy9k", description = "")
        productInteractor.add("13 Songs", 14, imageId = "HR9jN5Qxd", description = "")
        productInteractor.add("Computer World", 14, imageId = "wWZDwRbXt", description = "")
        productInteractor.add("Californication", 14, imageId = "cpyVcoBiB", description = "")
    }
}