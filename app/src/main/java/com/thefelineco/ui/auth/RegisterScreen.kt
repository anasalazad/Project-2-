package com.thefelineco.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thefelineco.di.AppViewModelProvider
import com.thefelineco.domain.CreditRules
import com.thefelineco.ui.components.PasswordField
import com.thefelineco.ui.components.ValidatedTextField

/** Create-account screen (stateful wrapper). */
@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RegisterContent(state, viewModel::onEvent, onBackToLogin)
}

@Composable
fun RegisterContent(state: RegisterUiState, onEvent: (RegisterEvent) -> Unit, onBackToLogin: () -> Unit) {
    AuthLayout(title = "Join The Feline Co.", subtitle = "Create your account to start your adoption journey.") {
        WelcomeBonusCallout()
        ValidatedTextField(
            value = state.fullName,
            onValueChange = { onEvent(RegisterEvent.FullNameChanged(it)) },
            label = "Full name",
            error = state.fullNameError,
            leadingIcon = Icons.Filled.Person,
        )
        ValidatedTextField(
            value = state.email,
            onValueChange = { onEvent(RegisterEvent.EmailChanged(it)) },
            label = "Email",
            error = state.emailError,
            leadingIcon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email,
        )
        PasswordField(
            value = state.password,
            onValueChange = { onEvent(RegisterEvent.PasswordChanged(it)) },
            label = "Password",
            error = state.passwordError,
            helper = "At least 8 characters, with a letter and a number",
        )
        PasswordField(
            value = state.confirmPassword,
            onValueChange = { onEvent(RegisterEvent.ConfirmChanged(it)) },
            label = "Confirm password",
            error = state.confirmError,
            imeAction = ImeAction.Done,
        )
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = state.acceptedTerms,
                        role = Role.Checkbox,
                        onValueChange = { onEvent(RegisterEvent.TermsChanged(it)) },
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = state.acceptedTerms, onCheckedChange = null)
                Text(
                    "I agree to the adoption terms and to care for my cat for life.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (state.termsError != null) {
                Text(
                    state.termsError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 48.dp),
                )
            }
        }
        AnimatedVisibility(visible = state.formError != null) {
            FormErrorBanner(state.formError.orEmpty())
        }
        SubmitButton(text = "Create account", loading = state.isSubmitting, onClick = { onEvent(RegisterEvent.Submit) })
        TextButton(onClick = onBackToLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Already have an account? Sign in")
        }
    }
}

@Composable
private fun WelcomeBonusCallout() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Redeem, contentDescription = null)
            Text(
                "New members get ${CreditRules.WELCOME_BONUS} welcome credits to spend on adoption fees or treats.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
