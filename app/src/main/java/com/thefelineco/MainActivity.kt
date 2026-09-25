package com.thefelineco

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thefelineco.di.AppViewModelProvider
import com.thefelineco.ui.MainViewModel
import com.thefelineco.ui.SessionState
import com.thefelineco.ui.auth.AuthNavHost
import com.thefelineco.ui.navigation.FelineAppShell
import com.thefelineco.ui.theme.FelineTheme
import com.thefelineco.ui.common.SplashScreen

/**
 * The main activity. It hosts every Compose screen through Navigation Compose, and launches
 * BookingActivity and CheckoutActivity through the Activity Result API.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { AppViewModelProvider.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        // The brand is dark-first, so the system bar icons are always light.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        setContent {
            FelineTheme {
                val session by viewModel.session.collectAsStateWithLifecycle()
                val basketCount by viewModel.basketCount.collectAsStateWithLifecycle()

                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
    }
}
