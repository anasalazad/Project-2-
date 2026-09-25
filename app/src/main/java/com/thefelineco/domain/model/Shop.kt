package com.thefelineco.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** An item for sale in the shop, priced in credits. */
data class Product(
    val id: Long = 0,
    val name: String,
    val brand: String,
    val category: ProductCategory,
    val priceCredits: Int,
    val description: String,
    val imageName: String,
    val stock: Int,
) {
    val inStock: Boolean get() = stock > 0
}

/**
 * One line in the basket. It is [Parcelable] so the whole basket can be passed to
 * CheckoutActivity as an `ArrayList<CartItem>`.
 */
@Parcelize
data class CartItem(
    val productId: Long,
    val name: String,
    val imageName: String,
    val unitPrice: Int,
    val quantity: Int,
    val stock: Int,
) : Parcelable {
    val lineTotal: Int get() = unitPrice * quantity
}

/** A completed shop purchase. CheckoutActivity returns it to MainActivity as a result. */
@Parcelize
data class Order(
    val id: Long = 0,
    val userId: Long,
    val items: List<CartItem>,
    val subtotal: Int,
    val deliveryMethod: DeliveryMethod,
    val deliveryName: String,
    val address: String,
    val createdAt: Long = System.currentTimeMillis(),
) : Parcelable {
    val total: Int get() = subtotal + deliveryMethod.extraCredits

    val itemCount: Int get() = items.sumOf { it.quantity }
}

/** Sum of every line in a basket. */
val List<CartItem>.subtotal: Int get() = sumOf { it.lineTotal }
