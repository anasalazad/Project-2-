package com.thefelineco.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thefelineco.data.local.FelineDatabase
import com.thefelineco.data.local.seed.DatabaseSeeder
import com.thefelineco.data.local.seed.SeedData
import com.thefelineco.data.repository.OfflineBookingRepository
import com.thefelineco.data.repository.OfflineCatRepository
import com.thefelineco.data.repository.OfflineShopRepository
import com.thefelineco.domain.CreditRules
import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.Booking
import com.thefelineco.domain.model.BookingStatus
import com.thefelineco.domain.model.DeliveryMethod
import com.thefelineco.domain.model.Order
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * End-to-end checks of the credit rules against a real (in-memory) Room database:
 * fee held on booking, refund on cancel, reward on adoption, and checkout.
 */
@RunWith(AndroidJUnit4::class)
class AdoptionAndShopFlowTest {
    private lateinit var db: FelineDatabase
    private lateinit var bookings: OfflineBookingRepository
    private lateinit var shop: OfflineShopRepository
    private var demoUserId = 0L

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, FelineDatabase::class.java).allowMainThreadQueries().build()
        DatabaseSeeder(db).seedIfEmpty()
        bookings = OfflineBookingRepository(db)
        shop = OfflineShopRepository(db)
        demoUserId = db.userDao().findByEmail(DatabaseSeeder.DEMO_EMAIL)!!.id
    }

    @After
    fun tearDown() = db.close()

    private suspend fun credits() = db.userDao().getById(demoUserId)!!.credits
    private suspend fun catNamed(name: String) = db.catDao().observeAll().first().first { it.name == name }

    private fun bookingFor(catId: Long, catName: String, slot: String = "11:00") = Booking(
        userId = demoUserId, catId = catId, catName = catName, catImageName = "",
        dateEpochDay = LocalDate.now().plusDays(3).toEpochDay(), timeSlot = slot, fullName = "Jordan Demo",
        email = DatabaseSeeder.DEMO_EMAIL, phone = "0412345678", homeType = "House", hasOtherPets = false,
        hasChildren = false, notes = "", feeCredits = 0,
    )

    @Test
    fun seedDataIsLoaded() = runTest {
        assertEquals(SeedData.cats.size, db.catDao().count())
        assertEquals(400, credits())
    }

    @Test
    fun bookingAKittenHoldsTheFeeAndCancellingRefundsIt() = runTest {
        val mochi = catNamed("Mochi")
        val booking = bookings.requestBooking(bookingFor(mochi.id, mochi.name)).getOrThrow()

        assertEquals(CreditRules.KITTEN_FEE, booking.feeCredits)
        assertEquals(400 - CreditRules.KITTEN_FEE, credits())
        assertEquals(AdoptionStatus.PENDING, catNamed("Mochi").status)

        bookings.cancel(booking.id).getOrThrow()
        assertEquals(400, credits())
        assertEquals(AdoptionStatus.AVAILABLE, catNamed("Mochi").status)
    }

    @Test
    fun completingAnAdoptionRewardsTheCustomer() = runTest {
        val archie = catNamed("Archie") // senior, free
        val booking = bookings.requestBooking(bookingFor(archie.id, archie.name)).getOrThrow()
        assertEquals(400, credits())

        bookings.confirm(booking.id).getOrThrow()
        bookings.completeAdoption(booking.id).getOrThrow()

        assertEquals(400 + CreditRules.ADOPTION_REWARD, credits())
        assertEquals(AdoptionStatus.ADOPTED, catNamed("Archie").status)
        val saved = bookings.observeBookingsForUser(demoUserId).first().single()
        assertEquals(BookingStatus.COMPLETED, saved.status)
    }

    @Test
    fun aPendingCatAndATakenSlotCannotBeBookedTwice() = runTest {
        val luna = catNamed("Luna")
        bookings.requestBooking(bookingFor(luna.id, luna.name, slot = "10:00")).getOrThrow()

        assertTrue(bookings.requestBooking(bookingFor(luna.id, luna.name, slot = "12:00")).isFailure)
        val nala = catNamed("Nala")
        assertTrue(bookings.requestBooking(bookingFor(nala.id, nala.name, slot = "10:00")).isFailure)
    }

    @Test
    fun checkoutChargesCreditsReducesStockAndEmptiesTheBasket() = runTest {
        val product = shop.observeProducts().first().first { it.stock >= 2 && it.priceCredits < 100 }
        shop.addToCart(demoUserId, product.id, 2).getOrThrow()
        val basket = shop.observeCart(demoUserId).first()

        val order = shop.checkout(
            Order(
                userId = demoUserId, items = basket, subtotal = 0, deliveryMethod = DeliveryMethod.EXPRESS,
                deliveryName = "Jordan Demo", address = "1 Test St, Carlton 3053",
            )
        ).getOrThrow()

        assertEquals(product.priceCredits * 2 + DeliveryMethod.EXPRESS.extraCredits, order.total)
        assertEquals(400 - order.total, credits())
        assertEquals(product.stock - 2, db.shopDao().getProduct(product.id)!!.stock)
        assertTrue(shop.observeCart(demoUserId).first().isEmpty())
    }

    @Test
    fun favouritesToggleAndAreRemovedWithTheCat() = runTest {
        val cats = OfflineCatRepository(db)
        val mochi = catNamed("Mochi")
        assertTrue(cats.toggleFavourite(demoUserId, mochi.id))
        assertEquals(setOf(mochi.id), cats.observeFavouriteIds(demoUserId).first())

        // Editing the cat (an upsert) must not drop the favourite.
        cats.saveCat(cats.getCat(mochi.id)!!.copy(colour = "Cream"))
        assertEquals(setOf(mochi.id), cats.observeFavouriteIds(demoUserId).first())

        cats.deleteCat(mochi.id).getOrThrow()
        assertTrue(cats.observeFavouriteIds(demoUserId).first().isEmpty())
    }

    @Test
    fun basketCannotExceedStock() = runTest {
        val outOfStock = shop.observeProducts().first().first { it.stock == 0 }
        assertTrue(shop.addToCart(demoUserId, outOfStock.id).isFailure)
    }
}
