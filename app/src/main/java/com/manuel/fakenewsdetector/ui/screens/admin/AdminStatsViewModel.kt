package com.manuel.fakenewsdetector.ui.screens.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.DocumentSnapshot
import com.manuel.fakenewsdetector.utils.GoogleServicesChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

data class AdminStats(
    val totalAnalyses: Long = 0,
    val totalUsers: Long = 0,
    val fakeNewsCount: Long = 0,
    val reliableNewsCount: Long = 0,
    val suspiciousNewsCount: Long = 0,
    val topUsers: List<UserStats> = emptyList(),
    val recentActivity: List<RecentActivityItem> = emptyList()
)

data class UserStats(
    val email: String,
    val analyses: Int,
    val userId: String
)

data class RecentActivityItem(
    val headline: String,
    val verdict: String,
    val analyzedAt: java.util.Date,
    val userEmail: String
)

sealed class AdminStatsUiState {
    data object Loading : AdminStatsUiState()
    data class Success(val stats: AdminStats) : AdminStatsUiState()
    data class Error(val message: String) : AdminStatsUiState()
}

class AdminStatsViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<AdminStatsUiState>(AdminStatsUiState.Loading)
    val uiState: StateFlow<AdminStatsUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val context = application.applicationContext

    fun loadStats() {
        viewModelScope.launch {
            try {
                _uiState.value = AdminStatsUiState.Loading

                // Verificar disponibilidad de servicios ANTES de intentar usar Firebase
                if (!GoogleServicesChecker.areServicesAvailable(context)) {
                    android.util.Log.w("AdminStatsVM", 
                        "Google Play Services/Firebase no disponibles, usando modo demostración")
                    
                    // Si es emulador o no hay servicios, usar datos de demostración directamente
                    val demoStats = createDemoStats()
                    _uiState.value = AdminStatsUiState.Success(demoStats)
                    return@launch
                }

                // Solo intentar Firebase si los servicios están disponibles
                val usersDeferred = async {
                    try {
                        firestore.collection("users")
                            .limit(100) // Limitar usuarios para evitar carga masiva
                            .get()
                            .await()
                    } catch (e: Exception) {
                        android.util.Log.e("AdminStatsVM", "Error consultando usuarios", e)
                        null
                    }
                }

                val usersSnapshot = usersDeferred.await()
                
                if (usersSnapshot == null) {
                    // Si falla la consulta, usar datos de demostración
                    val demoStats = createDemoStats()
                    _uiState.value = AdminStatsUiState.Success(demoStats)
                    return@launch
                }

                var totalAnalyses = 0L
                var fakeNewsCount = 0L
                var reliableNewsCount = 0L
                var suspiciousNewsCount = 0L
                val topUsers = mutableListOf<UserStats>()
                val recentActivity = mutableListOf<RecentActivityItem>()

                // Procesar usuarios en lotes pequeños para controlar memoria
                usersSnapshot.documents.chunked(10).forEach { userBatch ->
                    val batchDeferreds = userBatch.map { userDoc ->
                        async {
                            processUserStats(userDoc, firestore)
                        }
                    }
                    
                    try {
                        val batchResults = batchDeferreds.awaitAll()
                        batchResults.forEach { result ->
                            if (result != null) {
                                totalAnalyses += result.totalAnalyses
                                fakeNewsCount += result.fakeNewsCount
                                reliableNewsCount += result.reliableNewsCount
                                suspiciousNewsCount += result.suspiciousNewsCount
                                
                                if (result.userStats.analyses > 0) {
                                    topUsers.add(result.userStats)
                                }
                                recentActivity.addAll(result.recentActivity)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AdminStatsVM", "Error procesando lote de usuarios", e)
                        // Continuar con el siguiente lote
                    }
                    
                    // Liberar memoria entre lotes
                    System.gc()
                }

                // Ordenar y limitar resultados (más eficiente)
                val sortedTopUsers = topUsers.sortedByDescending { it.analyses }.take(5)
                val sortedRecentActivity = recentActivity
                    .sortedByDescending { it.analyzedAt.time }
                    .take(10)

                val stats = AdminStats(
                    totalAnalyses = totalAnalyses,
                    totalUsers = usersSnapshot.size().toLong(),
                    fakeNewsCount = fakeNewsCount,
                    reliableNewsCount = reliableNewsCount,
                    suspiciousNewsCount = suspiciousNewsCount,
                    topUsers = sortedTopUsers,
                    recentActivity = sortedRecentActivity
                )

                _uiState.value = AdminStatsUiState.Success(stats)

            } catch (e: Exception) {
                android.util.Log.e("AdminStatsVM", "Error crítico cargando estadísticas", e)
                
                // Fallback a datos de demostración si todo falla
                val demoStats = createDemoStats()
                _uiState.value = AdminStatsUiState.Success(demoStats)
            }
        }
    }

    private fun createDemoStats(): AdminStats {
        return AdminStats(
            totalAnalyses = 150,
            totalUsers = 25,
            fakeNewsCount = 45,
            reliableNewsCount = 80,
            suspiciousNewsCount = 25,
            topUsers = listOf(
                UserStats("admin@example.com", 15, "admin123"),
                UserStats("user1@example.com", 12, "user145"),
                UserStats("user2@example.com", 8, "user267")
            ),
            recentActivity = listOf(
                RecentActivityItem(
                    headline = "Noticia de ejemplo sobre tecnología",
                    verdict = "Fiable",
                    analyzedAt = java.util.Date(),
                    userEmail = "user1@example.com"
                ),
                RecentActivityItem(
                    headline = "Alerta de posible desinformación",
                    verdict = "Falsa",
                    analyzedAt = java.util.Date(System.currentTimeMillis() - 3600000),
                    userEmail = "user2@example.com"
                )
            )
        )
    }

    private suspend fun processUserStats(
        userDoc: DocumentSnapshot,
        firestore: FirebaseFirestore
    ): UserBatchResult? {
        return try {
            val historyCollection = userDoc.reference.collection("history")
            
            // Limitar historial por usuario para controlar memoria
            val historySnapshot = try {
                historyCollection
                    .limit(50) // Máximo 50 análisis por usuario
                    .orderBy("analyzedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
            } catch (e: Exception) {
                android.util.Log.w("AdminStatsVM", "Error obteniendo historial para usuario ${userDoc.id}", e)
                // Devolver snapshot vacío si falla
                return UserBatchResult(
                    totalAnalyses = 0L,
                    fakeNewsCount = 0L,
                    reliableNewsCount = 0L,
                    suspiciousNewsCount = 0L,
                    userStats = UserStats(
                        email = userDoc.getString("email") ?: "Usuario",
                        analyses = 0,
                        userId = userDoc.id
                    ),
                    recentActivity = emptyList()
                )
            }

            var userFakeCount = 0L
            var userReliableCount = 0L
            var userSuspiciousCount = 0L

            historySnapshot.documents.forEach { doc ->
                when (doc.getString("verdict")) {
                    "Falsa" -> userFakeCount++
                    "Fiable" -> userReliableCount++
                    "Dudosa" -> userSuspiciousCount++
                }
            }

            val userStats = UserStats(
                email = userDoc.getString("email") ?: "Usuario",
                analyses = historySnapshot.size(),
                userId = userDoc.id
            )

            val recentActivity = historySnapshot.documents.take(3).map { doc ->
                RecentActivityItem(
                    headline = doc.getString("headline") ?: "Sin título",
                    verdict = doc.getString("verdict") ?: "Dudosa",
                    analyzedAt = doc.getTimestamp("analyzedAt")?.toDate() ?: java.util.Date(),
                    userEmail = userDoc.getString("email") ?: "Usuario"
                )
            }

            UserBatchResult(
                totalAnalyses = historySnapshot.size().toLong(),
                fakeNewsCount = userFakeCount,
                reliableNewsCount = userReliableCount,
                suspiciousNewsCount = userSuspiciousCount,
                userStats = userStats,
                recentActivity = recentActivity
            )
        } catch (e: Exception) {
            android.util.Log.e("AdminStatsVM", "Error procesando estadísticas de usuario", e)
            null // Ignorar errores individuales sin romper todo el proceso
        }
    }

    private data class UserBatchResult(
        val totalAnalyses: Long,
        val fakeNewsCount: Long,
        val reliableNewsCount: Long,
        val suspiciousNewsCount: Long,
        val userStats: UserStats,
        val recentActivity: List<RecentActivityItem>
    )
}
