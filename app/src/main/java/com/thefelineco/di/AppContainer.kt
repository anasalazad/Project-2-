package com.thefelineco.di

import android.content.Context
import com.thefelineco.data.local.FelineDatabase
import com.thefelineco.data.local.seed.DatabaseSeeder
import com.thefelineco.data.repository.BookingRepository
import com.thefelineco.data.repository.CatRepository
import com.thefelineco.data.repository.DataStoreSessionStore
import com.thefelineco.data.repository.OfflineBookingRepository
import com.thefelineco.data.repository.OfflineCatRepository
import com.thefelineco.data.repository.OfflineShopRepository
import com.thefelineco.data.repository.OfflineUserRepository
import com.thefelineco.data.repository.ShopRepository
import com.thefelineco.data.repository.UserRepository

/**
 * Manual dependency injection: one place that creates and shares the app's singletons.
 * Every activity reaches it through [com.thefelineco.FelineApplication.container].
 */
interface AppContainer {
    val userRepository: UserRepository
    val catRepository: CatRepository
    val bookingRepository: BookingRepository
    val shopRepository: ShopRepository
    val seeder: DatabaseSeeder
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val database: FelineDatabase by lazy { FelineDatabase.build(context) }

    override val userRepository: UserRepository by lazy {
        OfflineUserRepository(database, DataStoreSessionStore(context))
    }
    override val catRepository: CatRepository by lazy { OfflineCatRepository(database) }
    override val bookingRepository: BookingRepository by lazy { OfflineBookingRepository(database) }
    override val shopRepository: ShopRepository by lazy { OfflineShopRepository(database) }
    override val seeder: DatabaseSeeder by lazy { DatabaseSeeder(database) }
}
