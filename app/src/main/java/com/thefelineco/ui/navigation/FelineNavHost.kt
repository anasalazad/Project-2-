package com.thefelineco.ui.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.thefelineco.domain.model.User
import com.thefelineco.ui.adopt.AdoptScreen
import com.thefelineco.ui.catdetail.CatDetailScreen
import com.thefelineco.ui.common.ComingSoonScreen
import com.thefelineco.domain.model.Booking
import com.thefelineco.ui.booking.BookMeetAndGreet
import com.thefelineco.ui.bookings.BookingsScreen
import com.thefelineco.ui.common.formatLong
import com.thefelineco.ui.components.SuccessDialog
import com.thefelineco.ui.home.HomeScreen

/** Every in-app screen for a signed-in user. Secondary activities are launched from these screens. */
@Composable
fun FelineNavHost(
    navController: NavHostController,
    user: User,
    onLogout: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = if (user.isAdmin) AdminDashboardRoute else HomeRoute,
    ) {
        // Customer
        composable<HomeRoute> {
            HomeScreen(
                onOpenCat = { catId -> navController.navigate(CatDetailRoute(catId)) },
                onBrowseCats = { freeOnly -> navController.navigate(AdoptRoute(freeOnly)) },
                onOpenShop = { navController.navigateToTopLevel(ShopRoute) },
            )
        }
        composable<AdoptRoute> {
            AdoptScreen(onOpenCat = { catId -> navController.navigate(CatDetailRoute(catId)) })
        }
        composable<CatDetailRoute> {
            // Activity Result API: launch BookingActivity with the Cat, receive the Booking back.
            var confirmed by rememberSaveable { mutableStateOf<Booking?>(null) }
            val bookLauncher = rememberLauncherForActivityResult(BookMeetAndGreet()) { booking ->
                confirmed = booking
            }
            CatDetailScreen(
                onBack = { navController.popBackStack() },
                onBook = { cat -> bookLauncher.launch(cat) },
                onOpenCat = { catId -> navController.navigate(CatDetailRoute(catId)) },
            )
            confirmed?.let { booking ->
                SuccessDialog(
                    title = "You're booked in!",
                    message = "Your meet & greet with ${booking.catName} is on ${booking.date.formatLong()} at " +
                        "${booking.timeSlot}. " + if (booking.feeCredits > 0) "${booking.feeCredits} credits are on hold." else "",
                    confirmLabel = "View my bookings",
                    onConfirm = {
                        confirmed = null
                        navController.navigateToTopLevel(BookingsRoute)
                    },
                    dismissLabel = "Keep browsing",
                    onDismiss = { confirmed = null },
                )
            }
        }
        composable<ShopRoute> { ComingSoonScreen("Shop", "Royal Feline food, toys and more. Arriving in Phase 4.") }
        composable<BasketRoute> { ComingSoonScreen("Basket", "Your basket and checkout arrive in Phase 4.") }
        composable<BookingsRoute> {
            BookingsScreen(onFindCat = { navController.navigateToTopLevel(AdoptRoute()) })
        }
        composable<ProfileRoute> {
            ComingSoonScreen(
                title = "Hi, ${user.firstName}",
                message = "${user.email} · ${user.credits} credits\nYour wallet and order history arrive in Phase 4.",
                actionLabel = "Log out",
                onAction = onLogout,
            )
        }

        // Admin
        composable<AdminDashboardRoute> { ComingSoonScreen("Admin dashboard", "Arriving in Phase 5.") }
        composable<AdminCatsRoute> { ComingSoonScreen("Manage cats", "Arriving in Phase 5.") }
        composable<AdminCatEditRoute> { ComingSoonScreen("Edit cat", "Arriving in Phase 5.") }
        composable<AdminBookingsRoute> { ComingSoonScreen("Appointments", "Arriving in Phase 5.") }
        composable<AdminProductsRoute> { ComingSoonScreen("Manage products", "Arriving in Phase 5.") }
        composable<AdminProductEditRoute> { ComingSoonScreen("Edit product", "Arriving in Phase 5.") }
    }
}
