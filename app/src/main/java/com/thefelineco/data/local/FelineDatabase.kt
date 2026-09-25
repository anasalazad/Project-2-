package com.thefelineco.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thefelineco.data.local.dao.BookingDao
import com.thefelineco.data.local.dao.CatDao
import com.thefelineco.data.local.dao.ShopDao
import com.thefelineco.data.local.dao.UserDao
import com.thefelineco.data.local.entity.BookingEntity
import com.thefelineco.data.local.entity.CartItemEntity
import com.thefelineco.data.local.entity.CatEntity
import com.thefelineco.data.local.entity.CreditTransactionEntity
import com.thefelineco.data.local.entity.OrderEntity
import com.thefelineco.data.local.entity.OrderItemEntity
import com.thefelineco.data.local.entity.ProductEntity
import com.thefelineco.data.local.entity.UserEntity

/** The app's single Room database. It is seeded on first launch by [com.thefelineco.data.local.seed.DatabaseSeeder]. */
@Database(
    entities = [
        UserEntity::class,
        CatEntity::class,
        BookingEntity::class,
        ProductEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        CreditTransactionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class FelineDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun catDao(): CatDao
    abstract fun bookingDao(): BookingDao
    abstract fun shopDao(): ShopDao

    companion object {
        fun build(context: Context): FelineDatabase =
            Room.databaseBuilder(context.applicationContext, FelineDatabase::class.java, "feline.db")
                // During development, wipe and re-seed instead of writing migrations.
                .fallbackToDestructiveMigration()
                .build()
    }
}
