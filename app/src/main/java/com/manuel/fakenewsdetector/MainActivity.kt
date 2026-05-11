package com.manuel.fakenewsdetector

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.manuel.fakenewsdetector.ui.screens.admin.AdminBlacklistScreen
import com.manuel.fakenewsdetector.ui.screens.admin.AdminHistoryScreen
import com.manuel.fakenewsdetector.ui.screens.admin.AdminStatsScreen
import com.manuel.fakenewsdetector.ui.screens.detail.AnalysisDetailScreen
import com.manuel.fakenewsdetector.ui.screens.history.HistoryScreen
import com.manuel.fakenewsdetector.ui.screens.login.AuthViewModel
import com.manuel.fakenewsdetector.ui.screens.login.LoginScreen
import com.manuel.fakenewsdetector.ui.screens.login.RegisterScreen
import com.manuel.fakenewsdetector.ui.screens.main.MainScreen
import com.manuel.fakenewsdetector.ui.screens.profile.ProfileScreen
import com.manuel.fakenewsdetector.ui.theme.FakeNewsDetectorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }

            FakeNewsDetectorTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                var isAdminMode by remember { mutableStateOf(false) }
                val authViewModel = remember { AuthViewModel() }

                // Function to upload profile photo
                fun uploadProfilePhoto(uri: Uri) {
                    lifecycleScope.launch {
                        try {
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            val uid = currentUser?.uid ?: return@launch
                            val storageRef = FirebaseStorage.getInstance()
                                .reference
                                .child("profile_photos/$uid.jpg")

                            storageRef.putFile(uri).await<com.google.firebase.storage.UploadTask.TaskSnapshot>()
                            val downloadUrl: android.net.Uri = storageRef.downloadUrl.await()
                            authViewModel.updatePhotoUrl(downloadUrl.toString())
                        } catch (e: Exception) {
                            // Handle error
                        }
                    }
                }

                // Check if user is already logged in
                val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
                    "main"
                } else {
                    "login"
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate("register")
                            }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToLogin = {
                                navController.navigateUp()
                            }
                        )
                    }

                    composable("main") {
                        MainScreen(
                            isAdminMode = isAdminMode,
                            onAnalyzeClick = { /* TODO: Abrir pantalla de análisis */ },
                            onNewsClick = { newsId ->
                                navController.navigate("detail/$newsId")
                            },
                            onNavigate = { route ->
                                when (route) {
                                    "history" -> navController.navigate("history")
                                    "profile" -> navController.navigate("profile")
                                    "admin/stats" -> navController.navigate("admin/stats")
                                }
                            }
                        )
                    }

                    composable("history") {
                        HistoryScreen(
                            onBackClick = { navController.navigateUp() },
                            onNewsClick = { newsId ->
                                navController.navigate("detail/$newsId")
                            }
                        )
                    }

                    composable("profile") {
                        ProfileScreen(
                            isAdminMode = isAdminMode,
                            onAdminModeToggle = { isAdminMode = it },
                            isDarkMode = isDarkMode,
                            onDarkModeToggle = { isDarkMode = it },
                            onLogoutClick = {
                                isAdminMode = false
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onEditProfileClick = { /* TODO */ },
                            onDeleteAccountClick = { /* TODO */ },
                            onNavigateToAdmin = { route ->
                                navController.navigate("admin/$route")
                            },
                            onBackClick = { navController.navigateUp() },
                            onPhotoSelected = { uri -> uploadProfilePhoto(uri) }
                        )
                    }

                    composable("detail/{newsId}") { backStackEntry ->
                        val newsId = backStackEntry.arguments?.getString("newsId") ?: ""
                        AnalysisDetailScreen(
                            analysisId = newsId,
                            onBackClick = { navController.navigateUp() }
                        )
                    }

                    // Admin routes
                    composable("admin/stats") {
                        AdminStatsScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }

                    composable("admin/history") {
                        AdminHistoryScreen(
                            onBackClick = { navController.navigateUp() },
                            onAnalysisClick = { newsId ->
                                navController.navigate("detail/$newsId")
                            }
                        )
                    }

                    composable("admin/blacklist") {
                        AdminBlacklistScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                }
            }
        }
    }
}
