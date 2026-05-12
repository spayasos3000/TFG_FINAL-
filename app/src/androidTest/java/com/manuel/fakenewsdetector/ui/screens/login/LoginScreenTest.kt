package com.manuel.fakenewsdetector.ui.screens.login

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.manuel.fakenewsdetector.ui.screens.login.LoginScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var loginSuccessCalled = false
    private var navigateToRegisterCalled = false

    @Before
    fun setup() {
        loginSuccessCalled = false
        navigateToRegisterCalled = false
    }

    @Test
    fun loginScreen_displaysCorrectly() {
        // Arrange
        val mockViewModel = AuthViewModel()

        // Act
        composeTestRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                LoginScreen(
                    onLoginSuccess = { loginSuccessCalled = true },
                    onNavigateToRegister = { navigateToRegisterCalled = true },
                    viewModel = mockViewModel
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Iniciar Sesión").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
    }

    @Test
    fun loginScreen_emailField_acceptsInput() {
        // Arrange
        val mockViewModel = AuthViewModel()
        val testEmail = "test@example.com"

        composeTestRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                LoginScreen(
                    onLoginSuccess = { loginSuccessCalled = true },
                    onNavigateToRegister = { navigateToRegisterCalled = true },
                    viewModel = mockViewModel
                )
            }
        }

        // Act
        composeTestRule.onNodeWithText("Email")
            .performTextInput(testEmail)

        // Assert - Verify the input was accepted (no crash)
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
    }

    @Test
    fun loginScreen_passwordField_acceptsInput() {
        // Arrange
        val mockViewModel = AuthViewModel()
        val testPassword = "password123"

        composeTestRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                LoginScreen(
                    onLoginSuccess = { loginSuccessCalled = true },
                    onNavigateToRegister = { navigateToRegisterCalled = true },
                    viewModel = mockViewModel
                )
            }
        }

        // Act
        composeTestRule.onNodeWithText("Contraseña")
            .performTextInput(testPassword)

        // Assert - Verify the input was accepted (no crash)
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
    }

    @Test
    fun loginScreen_registerButton_isClickable() {
        // Arrange
        val mockViewModel = AuthViewModel()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                LoginScreen(
                    onLoginSuccess = { loginSuccessCalled = true },
                    onNavigateToRegister = { navigateToRegisterCalled = true },
                    viewModel = mockViewModel
                )
            }
        }

        // Act
        composeTestRule.onNodeWithText("Registrarse")
            .performClick()

        // Assert
        assert(navigateToRegisterCalled)
    }
}
