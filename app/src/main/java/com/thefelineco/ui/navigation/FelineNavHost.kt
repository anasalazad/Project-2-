package com.thefelineco.ui.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.thefelineco.domain.model.Booking
import com.thefelineco.domain.model.DeliveryMethod
import com.thefelineco.domain.model.Order
import com.thefelineco.domain.model.User
import com.thefelineco.ui.admin.AdminBookingsScreen
import com.thefelineco.ui.admin.AdminCatsScreen
import com.thefelineco.ui.admin.AdminDashboardScreen
import com.thefelineco.ui.admin.AdminProductsScreen
import com.thefelineco.ui.admin.CatFormScreen
import com.thefelineco.ui.admin.ProductFormScreen
import com.thefelineco.ui.adopt.AdoptScreen
import com.thefelineco.ui.basket.BasketScreen
import com.thefelineco.ui.booking.BookMeetAndGreet
import com.thefelineco.ui.bookings.BookingsScreen
import com.thefelineco.ui.catdetail.CatDetailScreen
import com.thefelineco.ui.checkout.CheckoutContract
import com.thefelineco.ui.common.LocalSnackbarHostState
import com.thefelineco.ui.common.formatLong
import com.thefelineco.ui.components.SuccessDialog
import com.thefelineco.ui.home.HomeScreen
import com.thefelineco.ui.profile.ProfileScreen
import com.thefelineco.ui.shop.ShopScreen
import kotlinx.coroutines.launch

/** Every in-app screen for a signed-in user. Secondary activities are launched from these screens. */
@Composable
fun FelineNavHost(
    navController: NavHostController,
    user: User,
    onLogout: () -> Unit,
) {
    // Lives as long as the NavHost, so a message survives the screen that triggered it closing.
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val finishForm: (String) -> Unit = { message ->
        navController.popBackStack()
        scope.launch { snackbar.showSnackbar(message) }
    }

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
        composable<ShopRoute> { ShopScreen() }
        composable<BasketRoute> {
            // Activity Result API: launch CheckoutActivity with the basket, receive the Order back.
            var placed by rememberSaveable { mutableStateOf<Order?>(null) }
            val checkoutLauncher = rememberLauncherForActivityResult(CheckoutContract()) { order -> placed = order }
            BasketScreen(
                onCheckout = { items -> checkoutLauncher.launch(items) },
                onShop = { navController.navigateToTopLevel(ShopRoute) },
            )
            placed?.let { order ->
                SuccessDialog(
                    title = "Order placed!",
                    message = "Order #${order.id} · ${order.itemCount} item${if (order.itemCount == 1) "" else "s"} · " +
                        "${order.total} credits. " +
                        if (order.deliveryMethod == DeliveryMethod.CLICK_AND_COLLECT) "It'll be ready to collect tomorrow."
                        else "It's on its way to ${order.deliveryName}.",
                    confirmLabel = "View my orders",
                    onConfirm = {
                        placed = null
                        navController.navigateToTopLevel(ProfileRoute)
                    },
                    dismissLabel = "Keep shopping",
                    onDismiss = {
                        placed = null
                        navController.navigateToTopLevel(ShopRoute)
                    },
                )
            }
        }
        composable<BookingsRoute> {
            BookingsScreen(onFindCat = { navController.navigateToTopLevel(AdoptRoute()) })
        }
        composable<ProfileRoute> { ProfileScreen(onLogout = onLogout) }

        // Admin
        composable<AdminDashboardRoute> {
            AdminDashboardScreen(
                onAddCat = { navController.navigate(AdminCatEditRoute()) },
                onOpenAppointments = { navController.navigateToTopLevel(AdminBookingsRoute) },
                onOpenProducts = { navController.navigateToTopLevel(AdminProductsRoute) },
            )
        }
        composable<AdminCatsRoute> {
            AdminCatsScreen(
                onEditCat = { catId -> navController.navigate(AdminCatEditRoute(catId)) },
                onAddCat = { navController.navigate(AdminCatEditRoute()) },
            )
        }
        composable<AdminCatEditRoute> {
            CatFormScreen(onBack = { navController.popBackStack() }, onDone = finishForm)
        }
        composable<AdminBookingsRoute> { AdminBookingsScreen() }
        composable<AdminProductsRoute> {
            AdminProductsScreen(
                onEditProduct = { id -> navController.navigate(AdminProductEditRoute(id)) },
                onAddProduct = { navController.navigate(AdminProductEditRoute()) },
            )
        }
        composable<AdminProductEditRoute> {
            ProductFormScreen(onBack = { navController.popBackStack() }, onDone = finishForm)
        }
    }
}
