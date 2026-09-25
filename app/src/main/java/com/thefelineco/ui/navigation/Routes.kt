package com.thefelineco.ui.navigation

import kotlinx.serialization.Serializable

/*
 * Type-safe Navigation Compose routes. Arguments are constructor parameters, so the compiler
 * checks them (for example, CatDetailRoute always carries a catId).
 */

// Signed out
@Serializable data object LoginRoute
@Serializable data object RegisterRoute

// Customer
@Serializable data object HomeRoute
@Serializable data class AdoptRoute(val freeOnly: Boolean = false)
@Serializable data class CatDetailRoute(val catId: Long)
@Serializable data object ShopRoute
@Serializable data object BasketRoute
@Serializable data object BookingsRoute
@Serializable data object ProfileRoute

// Admin
@Serializable data object AdminDashboardRoute
@Serializable data object AdminCatsRoute
@Serializable data class AdminCatEditRoute(val catId: Long = 0)
@Serializable data object AdminBookingsRoute
@Serializable data object AdminProductsRoute
@Serializable data class AdminProductEditRoute(val productId: Long = 0)
