package com.thefelineco.ui.auth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thefelineco.ui.navigation.LoginRoute
import com.thefelineco.ui.navigation.RegisterRoute

/** Navigation for signed-out users: Login ⇄ Register. */
@Composable
fun AuthNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(onCreateAccount = { navController.navigate(RegisterRoute) { launchSingleTop = true } })
        }
        composable<RegisterRoute> {
            RegisterScreen(onBackToLogin = { navController.popBackStack() })
        }
    }
}
