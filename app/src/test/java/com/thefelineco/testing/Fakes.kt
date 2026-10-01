package com.thefelineco.testing

import com.thefelineco.data.repository.BookingRepository
import com.thefelineco.data.repository.CatRepository
import com.thefelineco.data.repository.FelineException
import com.thefelineco.data.repository.ShopRepository
import com.thefelineco.data.repository.UserRepository
import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.Booking
import com.thefelineco.domain.model.CartItem
import com.thefelineco.domain.model.Cat
import com.thefelineco.domain.model.CreditTransaction
import com.thefelineco.domain.model.Order
import com.thefelineco.domain.model.Product
import com.thefelineco.domain.model.User
import com.thefelineco.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/*
 * In-memory fakes of the repositories, so ViewModels can be tested without a database.
 * Each one records what it was asked to do.
 */

val testUser = User(id = 7, fullName = "Jordan Lee", email = "jordan@example.com", role = UserRole.CUSTOMER, credits = 400)

class FakeUserRepository(user: User? = testUser) : UserRepository {
    val user = MutableStateFlow(user)
    override val currentUser: Flow<User?> = this.user
    override suspend fun login(email: String, password: String): Result<User> = Result.failure(FelineException("not used"))
    override suspend fun register(fullName: String, email: String, password: String): Result<User> =
        Result.failure(FelineException("not used"))
    override suspend fun logout() { user.value = null }
    override fun observeTransactions(userId: Long): Flow<List<CreditTransaction>> = flowOf(emptyList())
}

class FakeCatRepository(cats: List<Cat> = emptyList()) : CatRepository {
    val cats = MutableStateFlow(cats)
    override fun observeCats(): Flow<List<Cat>> = cats
    override fun observeCat(id: Long): Flow<Cat?> = cats.map { list -> list.firstOrNull { it.id == id } }
    override suspend fun getCat(id: Long): Cat? = cats.value.firstOrNull { it.id == id }
    override suspend fun saveCat(cat: Cat): Long {
        val id = if (cat.id == 0L) (cats.value.maxOfOrNull { it.id } ?: 0) + 1 else cat.id
        cats.value = cats.value.filterNot { it.id == id } + cat.copy(id = id)
        return id
    }
    override suspend fun deleteCat(id: Long): Result<Unit> = runCatching { cats.value = cats.value.filterNot { it.id == id } }
    override suspend fun setStatus(id: Long, status: AdoptionStatus) {
        cats.value = cats.value.map { if (it.id == id) it.copy(status = status) else it }
    }

    /** userId → favourite cat ids. */
    val favourites = MutableStateFlow<Map<Long, Set<Long>>>(emptyMap())
    override fun observeFavouriteIds(userId: Long): Flow<Set<Long>> = favourites.map { it[userId].orEmpty() }
    override suspend fun toggleFavourite(userId: Long, catId: Long): Boolean {
        val current = favourites.value[userId].orEmpty()
        val nowFavourite = catId !in current
        favourites.value = favourites.value + (userId to if (nowFavourite) current + catId else current - catId)
        return nowFavourite
    }
}

class FakeBookingRepository : BookingRepository {
    val requested = mutableListOf<Booking>()
    var takenSlots: Map<Long, Set<String>> = emptyMap()
    var failWith: String? = null

    override fun observeBookingsForUser(userId: Long): Flow<List<Booking>> = flowOf(requested.filter { it.userId == userId })
    override fun observeAllBookings(): Flow<List<Booking>> = flowOf(requested)
    override suspend fun takenSlots(epochDay: Long): Set<String> = takenSlots[epochDay].orEmpty()
    override suspend fun requestBooking(booking: Booking): Result<Booking> {
        failWith?.let { return Result.failure(FelineException(it)) }
        val saved = booking.copy(id = requested.size + 1L)
        requested += saved
        return Result.success(saved)
    }
    override suspend fun cancel(bookingId: Long): Result<Unit> = Result.success(Unit)
    override suspend fun decline(bookingId: Long): Result<Unit> = Result.success(Unit)
    override suspend fun confirm(bookingId: Long): Result<Unit> = Result.success(Unit)
    override suspend fun completeAdoption(bookingId: Long): Result<Unit> = Result.success(Unit)
}

class FakeShopRepository(products: List<Product> = emptyList()) : ShopRepository {
    val products = MutableStateFlow(products)
    val placedOrders = mutableListOf<Order>()

    override fun observeProducts(): Flow<List<Product>> = products
    override fun observeProduct(id: Long): Flow<Product?> = products.map { list -> list.firstOrNull { it.id == id } }
    override suspend fun saveProduct(product: Product): Long = product.id
    override suspend fun deleteProduct(id: Long) = Unit
    override fun observeCart(userId: Long): Flow<List<CartItem>> = flowOf(emptyList())
    override suspend fun addToCart(userId: Long, productId: Long, quantity: Int): Result<Unit> = Result.success(Unit)
    override suspend fun setCartQuantity(userId: Long, productId: Long, quantity: Int): Result<Unit> = Result.success(Unit)
    override suspend fun checkout(order: Order): Result<Order> {
        val placed = order.copy(id = placedOrders.size + 1L)
        placedOrders += placed
        return Result.success(placed)
    }
    override fun observeOrders(userId: Long): Flow<List<Order>> = flowOf(placedOrders)
}
