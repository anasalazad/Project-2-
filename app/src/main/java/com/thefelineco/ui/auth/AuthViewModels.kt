package com.thefelineco.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefelineco.data.local.seed.DatabaseSeeder
import com.thefelineco.data.repository.UserRepository
import com.thefelineco.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ---------- Login ----------

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val formError: String? = null,
    val isSubmitting: Boolean = false,
)

sealed interface LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data object Submit : LoginEvent
    /** Fills in a demo account and signs in straight away. */
    data class UseDemo(val admin: Boolean) : LoginEvent
}

/**
 * Sign-in form. When sign-in succeeds, the session changes and MainActivity swaps to the app shell,
 * so there is no navigation event.
 */
class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            // Editing a field clears its error so the user isn't nagged while typing.
            is LoginEvent.EmailChanged -> _uiState.update { it.copy(email = event.value, emailError = null, formError = null) }
            is LoginEvent.PasswordChanged -> _uiState.update { it.copy(password = event.value, passwordError = null, formError = null) }
            LoginEvent.Submit -> submit()
            is LoginEvent.UseDemo -> {
                _uiState.update {
                    if (event.admin) it.copy(email = DatabaseSeeder.ADMIN_EMAIL, password = "Admin123!")
                    else it.copy(email = DatabaseSeeder.DEMO_EMAIL, password = "Demo123!")
                }
                submit()
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        val emailError = Validators.email(state.email)
        val passwordError = if (state.password.isEmpty()) "Password is required" else null
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, formError = null) }
        viewModelScope.launch {
            userRepository.login(state.email, state.password).onFailure { error ->
                _uiState.update { it.copy(isSubmitting = false, formError = error.message ?: "Couldn't sign in") }
            }
        }
    }
}

// ---------- Register ----------

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val acceptedTerms: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val termsError: String? = null,
    val formError: String? = null,
    val isSubmitting: Boolean = false,
)

sealed interface RegisterEvent {
    data class FullNameChanged(val value: String) : RegisterEvent
    data class EmailChanged(val value: String) : RegisterEvent
    data class PasswordChanged(val value: String) : RegisterEvent
    data class ConfirmChanged(val value: String) : RegisterEvent
    data class TermsChanged(val accepted: Boolean) : RegisterEvent
    data object Submit : RegisterEvent
}

/** Create-account form. New customers start with the welcome bonus. */
class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.FullNameChanged -> _uiState.update { it.copy(fullName = event.value, fullNameError = null) }
            is RegisterEvent.EmailChanged -> _uiState.update { it.copy(email = event.value, emailError = null, formError = null) }
            is RegisterEvent.PasswordChanged -> _uiState.update { it.copy(password = event.value, passwordError = null) }
            is RegisterEvent.ConfirmChanged -> _uiState.update { it.copy(confirmPassword = event.value, confirmError = null) }
            is RegisterEvent.TermsChanged -> _uiState.update { it.copy(acceptedTerms = event.accepted, termsError = null) }
            RegisterEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val s = _uiState.value
        if (s.isSubmitting) return
        val validated = s.copy(
            fullNameError = Validators.name(s.fullName),
            emailError = Validators.email(s.email),
            passwordError = Validators.password(s.password),
            confirmError = Validators.confirmPassword(s.password, s.confirmPassword),
            termsError = if (s.acceptedTerms) null else "Please accept the adoption terms",
        )
        val hasErrors = listOf(
            validated.fullNameError, validated.emailError, validated.passwordError,
            validated.confirmError, validated.termsError,
        ).any { it != null }
        if (hasErrors) {
            _uiState.value = validated
            return
        }
        _uiState.update { it.copy(isSubmitting = true, formError = null) }
        viewModelScope.launch {
            userRepository.register(s.fullName, s.email, s.password).onFailure { error ->
                _uiState.update { it.copy(isSubmitting = false, formError = error.message ?: "Couldn't create your account") }
            }
        }
    }
}
