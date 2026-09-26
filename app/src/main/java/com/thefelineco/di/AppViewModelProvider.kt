package com.thefelineco.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.thefelineco.FelineApplication
import com.thefelineco.ui.admin.AdminBookingsViewModel
import com.thefelineco.ui.admin.AdminCatsViewModel
import com.thefelineco.ui.admin.AdminDashboardViewModel
import com.thefelineco.ui.admin.AdminProductsViewModel
import com.thefelineco.ui.admin.CatFormViewModel
import com.thefelineco.ui.admin.ProductFormViewModel
import com.thefelineco.ui.adopt.AdoptViewModel
import com.thefelineco.ui.basket.BasketViewModel
import com.thefelineco.ui.bookings.BookingsViewModel
import com.thefelineco.ui.catdetail.CatDetailViewModel
import com.thefelineco.ui.navigation.AdminCatEditRoute
import com.thefelineco.ui.navigation.AdminProductEditRoute
import com.thefelineco.ui.navigation.AdoptRoute
import com.thefelineco.ui.navigation.CatDetailRoute
import com.thefelineco.ui.profile.ProfileViewModel
import com.thefelineco.ui.shop.ShopViewModel
import com.thefelineco.ui.MainViewModel
import com.thefelineco.ui.auth.LoginViewModel
import com.thefelineco.ui.auth.RegisterViewModel
import com.thefelineco.ui.home.HomeViewModel

/**
 * Creates every ViewModel with its dependencies from [AppContainer].
 * Usage: `viewModel(factory = AppViewModelProvider.Factory)`.
 */
object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { MainViewModel(container().userRepository, container().shopRepository) }
        initializer { LoginViewModel(container().userRepository) }
        initializer { RegisterViewModel(container().userRepository) }
        initializer {
            HomeViewModel(container().userRepository, container().catRepository, container().shopRepository)
        }
        initializer {
            // Route arguments arrive through the SavedStateHandle of the navigation back stack entry.
            AdoptViewModel(createSavedStateHandle().toRoute<AdoptRoute>().freeOnly, container().catRepository)
        }
        initializer {
            CatDetailViewModel(
                catId = createSavedStateHandle().toRoute<CatDetailRoute>().catId,
                catRepository = container().catRepository,
                userRepository = container().userRepository,
            )
        }
        initializer { BookingsViewModel(container().userRepository, container().bookingRepository) }
        initializer { ShopViewModel(container().userRepository, container().shopRepository) }
        initializer { BasketViewModel(container().userRepository, container().shopRepository) }
        initializer {
            ProfileViewModel(container().userRepository, container().shopRepository, container().settingsStore)
        }

        // Admin
        initializer {
            AdminDashboardViewModel(
                container().userRepository, container().catRepository,
                container().bookingRepository, container().shopRepository,
            )
        }
        initializer { AdminCatsViewModel(container().catRepository) }
        initializer { CatFormViewModel(createSavedStateHandle().toRoute<AdminCatEditRoute>().catId, container().catRepository) }
        initializer { AdminBookingsViewModel(container().bookingRepository) }
        initializer { AdminProductsViewModel(container().shopRepository) }
        initializer {
            ProductFormViewModel(createSavedStateHandle().toRoute<AdminProductEditRoute>().productId, container().shopRepository)
        }
    }
}

/** The app's [AppContainer], taken from the Application object inside the ViewModel's creation extras. */
fun CreationExtras.container(): AppContainer = (this[APPLICATION_KEY] as FelineApplication).container
