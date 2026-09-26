package com.thefelineco.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thefelineco.ui.auth.LoginContent
import com.thefelineco.ui.auth.LoginEvent
import com.thefelineco.ui.auth.LoginUiState
import com.thefelineco.ui.theme.FelineTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for the stateless login form. */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun fieldAndFormErrorsAreShown() {
        compose.setContent {
            FelineTheme {
                LoginContent(
                    state = LoginUiState(emailError = "Enter a valid email address", formError = "Incorrect email or password"),
                    onEvent = {},
                    onCreateAccount = {},
                )
            }
        }
        compose.onNodeWithText("Enter a valid email address").assertIsDisplayed()
        compose.onNodeWithText("Incorrect email or password").assertIsDisplayed()
    }

    @Test
    fun demoButtonsSendTheRightEvents() {
        val events = mutableListOf<LoginEvent>()
        compose.setContent {
            FelineTheme { LoginContent(state = LoginUiState(), onEvent = { events += it }, onCreateAccount = {}) }
        }
        compose.onNodeWithText("Admin", substring = true).performClick()
        compose.onNodeWithText("Sign in").performClick()
        assertTrue(events.contains(LoginEvent.UseDemo(admin = true)))
        assertTrue(events.contains(LoginEvent.Submit))
    }
}
