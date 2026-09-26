package com.thefelineco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thefelineco.di.AppViewModelProvider
import com.thefelineco.ui.MainViewModel
import com.thefelineco.ui.SessionState
import com.thefelineco.ui.auth.AuthNavHost
import com.thefelineco.ui.common.SplashScreen
import com.thefelineco.ui.common.setFelineContent
import com.thefelineco.ui.navigation.FelineAppShell

/**
 * The main activity. It hosts every Compose screen through Navigation Compose, and launches
 * BookingActivity and CheckoutActivity through the Activity Result API.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { AppViewModelProvider.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFelineContent {
            val session by viewModel.session.collectAsStateWithLifecycle()
            val basketCount by viewModel.basketCount.collectAsStateWithLifecycle()

            // Cross-fade between the splash, the sign-in flow and the signed-in app.
            AnimatedContent(
                targetState = session,
                contentKey = { it.contentKey },
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "session",
            ) { state ->
                when (state) {
                    SessionState.Loading -> SplashScreen()
                    SessionState.SignedOut -> AuthNavHost()
                    is SessionState.SignedIn -> FelineAppShell(
                        // Read the latest user (e.g. new credit balance), not the snapshot
                        // AnimatedContent captured when this account signed in.
                        user = (session as? SessionState.SignedIn)?.user ?: state.user,
                        basketCount = basketCount,
                        onLogout = viewModel::logout,
                    )
                }
            }
        }
    }
}
