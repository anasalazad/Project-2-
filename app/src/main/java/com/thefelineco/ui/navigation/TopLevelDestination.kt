package com.thefelineco.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.reflect.KClass

/**
 * An item in the navigation rail (tablet) or bottom bar (phone).
 *
 * @property route the route to open. @property routeClass used to highlight the selected item.
 */
enum class TopLevelDestination(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Any,
    val routeClass: KClass<*>,
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, HomeRoute, HomeRoute::class),
    ADOPT("Adopt", Icons.Filled.Pets, Icons.Outlined.Pets, AdoptRoute(), AdoptRoute::class),
    SHOP("Shop", Icons.Filled.Storefront, Icons.Outlined.Storefront, ShopRoute, ShopRoute::class),
    BASKET("Basket", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag, BasketRoute, BasketRoute::class),
    BOOKINGS("Bookings", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, BookingsRoute, BookingsRoute::class),
    ACCOUNT("Account", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, ProfileRoute, ProfileRoute::class),

    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, AdminDashboardRoute, AdminDashboardRoute::class),
    MANAGE_CATS("Cats", Icons.Filled.Pets, Icons.Outlined.Pets, AdminCatsRoute, AdminCatsRoute::class),
    APPOINTMENTS("Appointments", Icons.AutoMirrored.Filled.EventNote, Icons.AutoMirrored.Outlined.EventNote, AdminBookingsRoute, AdminBookingsRoute::class),
    PRODUCTS("Products", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, AdminProductsRoute, AdminProductsRoute::class);

    companion object {
        val customer = listOf(HOME, ADOPT, SHOP, BASKET, BOOKINGS, ACCOUNT)
        val admin = listOf(DASHBOARD, MANAGE_CATS, APPOINTMENTS, PRODUCTS, ACCOUNT)
    }
}
