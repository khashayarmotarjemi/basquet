package io.khashayar.domain.product

class AlbumProduct(
    val id: Long,
    val name: String,
    val price: Price,
    val releaseDate: String = "",
    val artistName: String = "",
    val imageUrl: String = "",
    val description: String = ""
) {

    override fun equals(other: Any?): Boolean {
        return other is AlbumProduct && this.hashCode() == other.hashCode()
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}

class Price(val amount: Int, val currency: String = "$") {
    fun plus(price: Price): Price {
        return Price(amount + price.amount, "$")
    }

    fun plus(addAmount: Int): Price {
        return Price(amount + addAmount, "$")
    }
}