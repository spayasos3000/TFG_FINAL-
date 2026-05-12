package com.manuel.fakenewsdetector

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.manuel.fakenewsdetector.ui.screens.admin.AdminUsersScreen
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
            // ✅ AuthViewModel compartido a nivel de Activity (no se recrea en navegaciones)
            val authViewModel: AuthViewModel = viewModel(viewModelStoreOwner = this)
            val userData by authViewModel.userData.collectAsState()
            val isAdmin = userData?.role == "admin"

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
                            },
                            viewModel = authViewModel
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
                            },
                            viewModel = authViewModel
                        )
                    }

                    composable("main") {
                        MainScreen(
                            isAdmin = isAdmin,
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
                            userData = userData,
                            isDarkMode = isDarkMode,
                            onDarkModeToggle = { isDarkMode = it },
                            onLogoutClick = {
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            },
                            onEditProfileClick = { /* TODO */ },
                            onDeleteAccountClick = {
                                authViewModel.deleteAccount(
                                    onSuccess = {
                                        navController.navigate("login") {
                                            popUpTo("main") { inclusive = true }
                                        }
                                    },
                                    onError = { error ->
                                        // TODO: Mostrar error al usuario
                                        android.util.Log.e("MainActivity", "Error eliminando cuenta: $error")
                                    }
                                )
                            },
                            onNavigateToAdmin = { route ->
                                navController.navigate("admin/$route")
                            },
                            onBackClick = { navController.navigateUp() },
                            onPhotoSelected = { uri -> uploadProfilePhoto(uri) },
                            viewModel = authViewModel
                        )
                    }

                    composable("detail/{newsId}") { backStackEntry ->
                        val newsId = backStackEntry.arguments?.getString("newsId") ?: ""
                        AnalysisDetailScreen(
                            analysisId = newsId,
                            onBackClick = { navController.navigateUp() }
                        )
                    }

                    // Admin routes - protegidas por role
                    composable("admin/stats") {
                        if (isAdmin) {
                            AdminStatsScreen(
                                onBackClick = { navController.navigateUp() }
                            )
                        } else {
                            // Redirigir a main si no es admin
                            navController.navigate("main") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    }

                    composable("admin/history") {
                        if (isAdmin) {
                            AdminHistoryScreen(
                                onBackClick = { navController.navigateUp() },
                                onAnalysisClick = { newsId ->
                                    navController.navigate("detail/$newsId")
                                }
                            )
                        } else {
                            navController.navigate("main") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    }

                    composable("admin/blacklist") {
                        if (isAdmin) {
                            AdminBlacklistScreen(
                                onBackClick = { navController.navigateUp() }
                            )
                        } else {
                            navController.navigate("main") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    }

                    composable("admin/users") {
                        if (isAdmin) {
                            AdminUsersScreen(
                                onBackClick = { navController.navigateUp() },
                                onRoleChange = { uid, newRole ->
                                    authViewModel.changeUserRole(uid, newRole, 
                                        onSuccess = { /* Recargar lista */ },
                                        onError = { /* Mostrar error */ }
                                    )
                                },
                                viewModel = authViewModel
                            )
                        } else {
                            navController.navigate("main") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
}
