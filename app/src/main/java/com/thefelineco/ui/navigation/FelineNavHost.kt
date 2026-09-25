package com.thefelineco.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.thefelineco.domain.model.User
import com.thefelineco.ui.common.ComingSoonScreen
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
        composable<AdoptRoute> { ComingSoonScreen("Adopt", "Search, filter and sort every cat. Arriving in Phase 2.") }
        composable<CatDetailRoute> { ComingSoonScreen("Cat profile", "Full profiles and booking arrive in Phase 2.") }
        composable<ShopRoute> { ComingSoonScreen("Shop", "Royal Feline food, toys and more. Arriving in Phase 4.") }
        composable<BasketRoute> { ComingSoonScreen("Basket", "Your basket and checkout arrive in Phase 4.") }
        composable<BookingsRoute> { ComingSoonScreen("My bookings", "Your meet & greet appointments arrive in Phase 3.") }
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
