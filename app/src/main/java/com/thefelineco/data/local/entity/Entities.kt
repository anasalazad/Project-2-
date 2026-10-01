package com.thefelineco.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.BookingStatus
import com.thefelineco.domain.model.CoatLength
import com.thefelineco.domain.model.DeliveryMethod
import com.thefelineco.domain.model.ProductCategory
import com.thefelineco.domain.model.Sex
import com.thefelineco.domain.model.TransactionType
import com.thefelineco.domain.model.UserRole

/*
 * Room tables. They are kept separate from the domain models so the database schema can change
 * without touching the UI. Mappers live in Mappers.kt.
 */

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val role: UserRole,
    val credits: Int,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "cats")
data class CatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val breed: String,
    val ageMonths: Int,
    val sex: Sex,
    val coat: CoatLength,
    val colour: String,
    val weightKg: Double,
    val personality: List<String>,
    val description: String,
    val imageName: String,
    val goodWithKids: Boolean,
    val goodWithCats: Boolean,
    val goodWithDogs: Boolean,
    val indoorOnly: Boolean,
    val vaccinated: Boolean,
    val desexed: Boolean,
    val microchipped: Boolean,
    val specialNeeds: String?,
    val status: AdoptionStatus,
    val listedAt: Long,
)

/** No foreign key to cats: bookings keep a snapshot of the cat's name and photo for history. */
@Entity(tableName = "bookings", indices = [Index("userId"), Index("catId")])
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val catId: Long,
    val catName: String,
    val catImageName: String,
    val dateEpochDay: Long,
    val timeSlot: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val homeType: String,
    val hasOtherPets: Boolean,
    val hasChildren: Boolean,
    val notes: String,
    val feeCredits: Int,
    val status: BookingStatus,
    val createdAt: Long,
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String,
    val category: ProductCategory,
    val priceCredits: Int,
    val description: String,
    val imageName: String,
    val stock: Int,
    /** Position in the curated "Featured" order. */
    val sortOrder: Int = 0,
)

@Entity(
    tableName = "cart_items",
    primaryKeys = ["userId", "productId"],
    indices = [Index("productId")],
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CartItemEntity(
    val userId: Long,
    val productId: Long,
    val quantity: Int,
)

@Entity(tableName = "orders", indices = [Index("userId")])
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val subtotal: Int,
    val deliveryMethod: DeliveryMethod,
    val deliveryName: String,
    val address: String,
    val createdAt: Long,
)

/** Order lines copy the product name and price so past orders never change. */
@Entity(
    tableName = "order_items",
    indices = [Index("orderId")],
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val name: String,
    val imageName: String,
    val unitPrice: Int,
    val quantity: Int,
)

/** An order together with its lines, loaded in one query with @Relation. */
data class OrderWithItems(
    @Embedded val order: OrderEntity,
    @Relation(parentColumn = "id", entityColumn = "orderId")
    val items: List<OrderItemEntity>,
)

@Entity(tableName = "credit_transactions", indices = [Index("userId")])
data class CreditTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val amount: Int,
    val type: TransactionType,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * A cat a customer has hearted. Deleting the cat removes its favourites (CASCADE).
 * Cats are saved with @Upsert (insert-or-update), so editing a cat never triggers the cascade.
 */
@Entity(
    tableName = "favourites",
    primaryKeys = ["userId", "catId"],
    indices = [Index("catId")],
    foreignKeys = [
        ForeignKey(
            entity = CatEntity::class,
            parentColumns = ["id"],
            childColumns = ["catId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class FavouriteEntity(
    val userId: Long,
    val catId: Long,
    val createdAt: Long = System.currentTimeMillis(),
)

/** Result row of the basket query (cart line joined with its product). */
data class CartLine(
    val productId: Long,
    val name: String,
    val imageName: String,
    val unitPrice: Int,
    val quantity: Int,
    val stock: Int,
)
