package com.thefelineco.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.thefelineco.data.local.entity.CartItemEntity
import com.thefelineco.data.local.entity.CartLine
import com.thefelineco.data.local.entity.OrderEntity
import com.thefelineco.data.local.entity.OrderItemEntity
import com.thefelineco.data.local.entity.OrderWithItems
import com.thefelineco.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/** Products, basket and orders. */
@Dao
interface ShopDao {
    // Products
    @Query("SELECT * FROM products ORDER BY sortOrder, name")
    fun observeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun observeProduct(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProduct(id: Long): ProductEntity?

    @Upsert
    suspend fun upsertProduct(product: ProductEntity): Long

    @Insert
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Long)

    @Query("UPDATE products SET stock = stock - :quantity WHERE id = :id")
    suspend fun reduceStock(id: Long, quantity: Int)

    @Query("SELECT MAX(sortOrder) FROM products")
    suspend fun maxSortOrder(): Int?

    // Basket
    @Query(
        """
        SELECT c.productId AS productId, p.name AS name, p.imageName AS imageName,
               p.priceCredits AS unitPrice, c.quantity AS quantity, p.stock AS stock
        FROM cart_items c INNER JOIN products p ON p.id = c.productId
        WHERE c.userId = :userId
        ORDER BY p.name
        """
    )
    fun observeCart(userId: Long): Flow<List<CartLine>>

    @Query("SELECT quantity FROM cart_items WHERE userId = :userId AND productId = :productId")
    suspend fun cartQuantity(userId: Long, productId: Long): Int?

    @Upsert
    suspend fun upsertCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE userId = :userId AND productId = :productId")
    suspend fun removeCartItem(userId: Long, productId: Long)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: Long)

    // Orders
    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Transaction
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeOrders(userId: Long): Flow<List<OrderWithItems>>
}
