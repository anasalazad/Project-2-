package com.thefelineco.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.thefelineco.FelineApplication
import com.thefelineco.ui.adopt.AdoptViewModel
import com.thefelineco.ui.catdetail.CatDetailViewModel
import com.thefelineco.ui.navigation.AdoptRoute
import com.thefelineco.ui.navigation.CatDetailRoute
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
    }
}

/** The app's [AppContainer], taken from the Application object inside the ViewModel's creation extras. */
fun CreationExtras.container(): AppContainer = (this[APPLICATION_KEY] as FelineApplication).container
