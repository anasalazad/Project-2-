package com.thefelineco.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thefelineco.di.AppViewModelProvider
import com.thefelineco.ui.components.PasswordField
import com.thefelineco.ui.components.ValidatedTextField

/** Sign-in screen (stateful wrapper). */
@Composable
fun LoginScreen(
    onCreateAccount: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LoginContent(state = state, onEvent = viewModel::onEvent, onCreateAccount = onCreateAccount)
}

/** Stateless sign-in form, easy to preview and test. */
@Composable
fun LoginContent(state: LoginUiState, onEvent: (LoginEvent) -> Unit, onCreateAccount: () -> Unit) {
    AuthLayout(title = "Welcome back", subtitle = "Sign in to adopt, shop and track your bookings.") {
        ValidatedTextField(
            value = state.email,
            onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
            label = "Email",
            error = state.emailError,
            leadingIcon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email,
        )
        PasswordField(
            value = state.password,
            onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
            label = "Password",
            error = state.passwordError,
            imeAction = ImeAction.Done,
            onImeAction = { onEvent(LoginEvent.Submit) },
        )
        AnimatedVisibility(visible = state.formError != null) {
            FormErrorBanner(state.formError.orEmpty())
        }
        SubmitButton(text = "Sign in", loading = state.isSubmitting, onClick = { onEvent(LoginEvent.Submit) })

        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f))
            Text(
                "  or explore with a demo account  ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider(Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { onEvent(LoginEvent.UseDemo(admin = false)) },
                enabled = !state.isSubmitting,
                modifier = Modifier.weight(1f).height(52.dp),
            ) {
                Icon(Icons.Filled.Pets, null, Modifier.size(18.dp))
                Text("  Customer")
            }
            OutlinedButton(
                onClick = { onEvent(LoginEvent.UseDemo(admin = true)) },
                enabled = !state.isSubmitting,
                modifier = Modifier.weight(1f).height(52.dp),
            ) {
                Icon(Icons.Filled.AdminPanelSettings, null, Modifier.size(18.dp))
                Text("  Admin")
            }
        }
        TextButton(onClick = onCreateAccount, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("New to The Feline Co.? Create an account")
        }
    }
}

/** Full-width primary button that shows a spinner while [loading]. */
@Composable
fun SubmitButton(text: String, loading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, enabled = !loading, modifier = modifier.fillMaxWidth().height(56.dp)) {
        Box(contentAlignment = Alignment.Center) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(text, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
