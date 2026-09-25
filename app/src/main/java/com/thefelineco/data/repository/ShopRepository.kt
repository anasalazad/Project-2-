package com.thefelineco.data.repository

import androidx.room.withTransaction
import com.thefelineco.data.local.FelineDatabase
import com.thefelineco.data.local.entity.CartItemEntity
import com.thefelineco.data.local.entity.OrderEntity
import com.thefelineco.data.local.entity.OrderItemEntity
import com.thefelineco.data.local.entity.toDomain
import com.thefelineco.data.local.entity.toEntity
import com.thefelineco.data.local.recordCredits
import com.thefelineco.domain.CreditRules
import com.thefelineco.domain.model.CartItem
import com.thefelineco.domain.model.Order
import com.thefelineco.domain.model.Product
import com.thefelineco.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Products, basket and checkout. */
interface ShopRepository {
    fun observeProducts(): Flow<List<Product>>
    fun observeProduct(id: Long): Flow<Product?>
    suspend fun saveProduct(product: Product): Long
    suspend fun deleteProduct(id: Long)

    fun observeCart(userId: Long): Flow<List<CartItem>>

    /** Adds [quantity] of a product to the basket, respecting the stock level. */
    suspend fun addToCart(userId: Long, productId: Long, quantity: Int = 1): Result<Unit>

    /** Sets a basket line's quantity. Zero removes it. */
    suspend fun setCartQuantity(userId: Long, productId: Long, quantity: Int): Result<Unit>

    /** Places the order: checks stock and credits, charges, reduces stock and empties the basket. */
    suspend fun checkout(order: Order): Result<Order>

    fun observeOrders(userId: Long): Flow<List<Order>>
}

class OfflineShopRepository(private val db: FelineDatabase) : ShopRepository {
    private val shopDao = db.shopDao()

    override fun observeProducts(): Flow<List<Product>> =
        shopDao.observeProducts().map { list -> list.map { it.toDomain() } }

    override fun observeProduct(id: Long): Flow<Product?> = shopDao.observeProduct(id).map { it?.toDomain() }

    override suspend fun saveProduct(product: Product): Long {
        val existing = shopDao.getProduct(product.id)
        val sortOrder = existing?.sortOrder ?: ((shopDao.maxSortOrder() ?: 0) + 1)
        val rowId = shopDao.upsertProduct(product.toEntity(sortOrder))
        return if (rowId == -1L) product.id else rowId
    }

    override suspend fun deleteProduct(id: Long) = shopDao.deleteProduct(id)

    override fun observeCart(userId: Long): Flow<List<CartItem>> =
        shopDao.observeCart(userId).map { lines -> lines.map { it.toDomain() } }

    override suspend fun addToCart(userId: Long, productId: Long, quantity: Int): Result<Unit> = runCatching {
        val current = shopDao.cartQuantity(userId, productId) ?: 0
        setQuantityChecked(userId, productId, current + quantity)
    }

    override suspend fun setCartQuantity(userId: Long, productId: Long, quantity: Int): Result<Unit> = runCatching {
        setQuantityChecked(userId, productId, quantity)
    }

    private suspend fun setQuantityChecked(userId: Long, productId: Long, quantity: Int) {
        if (quantity <= 0) {
            shopDao.removeCartItem(userId, productId)
            return
        }
        val product = shopDao.getProduct(productId) ?: throw FelineException("This product is no longer available")
        if (product.stock <= 0) throw FelineException("${product.name} is out of stock")
        if (quantity > product.stock) throw FelineException("Only ${product.stock} left in stock")
        shopDao.upsertCartItem(CartItemEntity(userId, productId, quantity))
    }

    override suspend fun checkout(order: Order): Result<Order> = runCatching {
        if (order.items.isEmpty()) throw FelineException("Your basket is empty")
        db.withTransaction {
            val user = db.userDao().getById(order.userId) ?: throw FelineException("Please sign in again")

            // Re-price and re-check stock from the database: the basket may be out of date.
            val lines = order.items.map { item ->
                val product = shopDao.getProduct(item.productId)
                    ?: throw FelineException("${item.name} is no longer available")
                if (item.quantity > product.stock) {
                    throw FelineException("Only ${product.stock} × ${product.name} left in stock")
                }
                item.copy(unitPrice = product.priceCredits, name = product.name)
            }
            val placed = order.copy(items = lines, subtotal = lines.sumOf { it.lineTotal })
            if (!CreditRules.canAfford(user.credits, placed.total)) {
                throw FelineException("You need ${placed.total} credits but have ${user.credits}")
            }

            val orderId = shopDao.insertOrder(
                OrderEntity(
                    userId = placed.userId,
                    subtotal = placed.subtotal,
                    deliveryMethod = placed.deliveryMethod,
                    deliveryName = placed.deliveryName.trim(),
                    address = placed.address.trim(),
                    createdAt = placed.createdAt,
                )
            )
            shopDao.insertOrderItems(
                lines.map { OrderItemEntity(0, orderId, it.productId, it.name, it.imageName, it.unitPrice, it.quantity) }
            )
            lines.forEach { shopDao.reduceStock(it.productId, it.quantity) }
            db.recordCredits(
                user.id,
                -placed.total,
                TransactionType.SHOP_PURCHASE,
                "Order #$orderId · ${placed.itemCount} item${if (placed.itemCount == 1) "" else "s"}",
            )
            shopDao.clearCart(user.id)
            placed.copy(id = orderId)
        }
    }

    override fun observeOrders(userId: Long): Flow<List<Order>> =
        shopDao.observeOrders(userId).map { list -> list.map { it.toDomain() } }
}
