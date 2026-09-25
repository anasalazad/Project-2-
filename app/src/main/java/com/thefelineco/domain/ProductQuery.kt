package com.thefelineco.domain

import com.thefelineco.domain.model.Product
import com.thefelineco.domain.model.ProductCategory

/** Sort orders offered in the shop. */
enum class ProductSort(val label: String) {
    FEATURED("Featured"),
    PRICE_LOW("Price: low to high"),
    PRICE_HIGH("Price: high to low"),
    NAME("Name (A–Z)"),
}

/** Search, category filter and sort for the shop. A null [category] means all categories. */
data class ProductQuery(
    val text: String = "",
    val category: ProductCategory? = null,
    val inStockOnly: Boolean = false,
    val sort: ProductSort = ProductSort.FEATURED,
) {
    fun apply(products: List<Product>): List<Product> {
        val needle = text.trim()
        val filtered = products.filter { p ->
            (category == null || p.category == category) &&
                (!inStockOnly || p.inStock) &&
                (needle.isEmpty() || p.name.contains(needle, true) ||
                    p.brand.contains(needle, true) || p.category.label.contains(needle, true))
        }
        return when (sort) {
            ProductSort.FEATURED -> filtered // seed order is the curated "featured" order
            ProductSort.PRICE_LOW -> filtered.sortedBy { it.priceCredits }
            ProductSort.PRICE_HIGH -> filtered.sortedByDescending { it.priceCredits }
            ProductSort.NAME -> filtered.sortedBy { it.name.lowercase() }
        }
    }
}
