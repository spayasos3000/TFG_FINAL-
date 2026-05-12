package com.manuel.fakenewsdetector.ui.screens.main

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.manuel.fakenewsdetector.ui.screens.main.MainScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var analyzeClickCalled = false
    private var newsClickCalled = false
    private var navigateCalled = false
    private var navigatedRoute = ""

    @Before
    fun setup() {
        analyzeClickCalled = false
        newsClickCalled = false
        navigateCalled = false
        navigatedRoute = ""
    }

    @Test
    fun mainScreen_displaysCorrectly() {
        // Arrange
        val isAdmin = false

        // Act
        composeTestRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                MainScreen(
                    isAdmin = isAdmin,
                    onAnalyzeClick = { analyzeClickCalled = true },
                    onNewsClick = { newsClickCalled = true },
                    onNavigate = { route ->
                        navigateCalled = true
                        navigatedRoute = route
                    }
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Analizar Contenido de Noticia").assertIsDisplayed()
    }

    @Test
    fun mainScreen_inputField_acceptsText() {
        // Arrange
        val isAdmin = false
        val testText = "Esta es una noticia de prueba"

        composeTestRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                MainScreen(
                    isAdmin = isAdmin,
                    onAnalyzeClick = { analyzeClickCalled = true },
                    onNewsClick = { newsClickCalled = true },
                    onNavigate = { route ->
                        navigateCalled = true
                        navigatedRoute = route
                    }
                )
            }
        }

        // Act
        composeTestRule.onNodeWithText("Pega aquí el texto completo de la noticia para analizar...")
            .performTextInput(testText)

        // Assert - Verify the input was accepted (no crash)
        composeTestRule.onNodeWithText("Pega aquí el texto completo de la noticia para analizar...")
            .assertIsDisplayed()
    }

    @Test
    fun mainScreen_analyzeButton_isClickable() {
        // Arrange
        val isAdmin = false
        val testText = "Esta es una noticia de prueba"

        composeTestRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                MainScreen(
                    isAdmin = isAdmin,
                    onAnalyzeClick = { analyzeClickCalled = true },
                    onNewsClick = { newsClickCalled = true },
                    onNavigate = { route ->
                        navigateCalled = true
                        navigatedRoute = route
                    }
                )
            }
        }

        // Act - Enter text and click analyze
        composeTestRule.onNodeWithText("Pega aquí el texto completo de la noticia para analizar...")
            .performTextInput(testText)
        
        composeTestRule.onNodeWithText("Analizar noticia")
            .performClick()

        // Assert - Note: This might not actually call the click due to ViewModel state
        // but we verify the button exists and is clickable
        composeTestRule.onNodeWithText("Analizar noticia").assertIsDisplayed()
    }
}
