package com.manuel.fakenewsdetector.ui.screens.main

import android.Manifest
import android.app.Application
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.manuel.fakenewsdetector.data.repository.NewsRepository
import com.manuel.fakenewsdetector.domain.model.AnalysisResult
import com.manuel.fakenewsdetector.notifications.AppNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.System.currentTimeMillis

sealed class MainUiState {
    data object Idle : MainUiState()
    data object Loading : MainUiState()
    data class Success(val result: AnalysisResult) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Idle)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val repository = NewsRepository()
    private val notificationManager = AppNotificationManager(application)
    
    // Control de ritmo: última llamada y delay mínimo
    private var lastAnalysisTime: Long = 0
    private val MIN_DELAY_BETWEEN_CALLS = 2000L // 2 segundos mínimo entre llamadas
    private var isProcessing = false

    fun analyze(input: String) {
        // Evitar llamadas múltiples simultáneas
        if (isProcessing) return
        
        // Controlar ritmo: mínimo 2 segundos entre llamadas
        val currentTime = currentTimeMillis()
        val timeSinceLastCall = currentTime - lastAnalysisTime
        if (timeSinceLastCall < MIN_DELAY_BETWEEN_CALLS && lastAnalysisTime > 0) {
            val remainingDelay = MIN_DELAY_BETWEEN_CALLS - timeSinceLastCall
            viewModelScope.launch {
                delay(remainingDelay)
                performAnalysis(input)
            }
            return
        }
        
        performAnalysis(input)
    }
    
    private fun performAnalysis(input: String) {
        isProcessing = true
        lastAnalysisTime = currentTimeMillis()
        
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            try {
                val result = withContext(Dispatchers.IO) {
                    repository.analyzeNews(input)
                }
                _uiState.value = MainUiState.Success(result)
                saveToFirestore(input, result)
                
                // Mostrar notificación del análisis completado (solo si tenemos permisos)
                val headline = input.takeIf { it.length < 100 } ?: input.substring(0, 100)
                try {
                    if (notificationManager.hasNotificationPermission()) {
                        notificationManager.showAnalysisCompleteNotification(
                            headline = headline,
                            verdict = result.verdict,
                            confidenceScore = result.confidenceScore
                        )
                        
                        // Si es noticia falsa, mostrar alerta adicional
                        if (result.verdict == com.manuel.fakenewsdetector.domain.model.Verdict.FALSA) {
                            notificationManager.showFakeNewsAlert(
                                headline = headline,
                                explanation = result.explanation
                            )
                        }
                    }
                } catch (e: SecurityException) {
                    // Silenciosamente ignorar errores de permisos de notificación
                    // Las notificaciones son opcionales, no deben romper la funcionalidad principal
                }
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(
                    e.message ?: "Error al analizar la noticia"
                )
            } finally {
                isProcessing = false
            }
        }
    }

    private fun saveToFirestore(
        input: String, 
        result: AnalysisResult
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("history")
                    .add(
                        hashMapOf(
                            "headline" to (input.takeIf { it.length < 100 } ?: input.substring(0, 100)),
                            "url" to if (input.startsWith("http")) input else null,
                            "content" to input,
                            "verdict" to when (result.verdict) {
                                com.manuel.fakenewsdetector.domain.model.Verdict.FIABLE -> "Fiable"
                                com.manuel.fakenewsdetector.domain.model.Verdict.DUDOSA -> "Dudosa"
                                com.manuel.fakenewsdetector.domain.model.Verdict.FALSA -> "Falsa"
                            },
                            "confidenceScore" to result.confidenceScore,
                            "explanation" to result.explanation,
                            "detectedPatterns" to result.detectedPatterns,
                            "sourcesChecked" to result.sourcesChecked,
                            "similarReliableArticles" to result.similarReliableArticles,
                            "blacklistedDomainMatch" to result.blacklistedDomainMatch,
                            "alternativeNewsTitle" to result.alternativeNewsTitle,
                            "alternativeNewsUrl" to result.alternativeNewsUrl,
                            "alternativeNewsDescription" to result.alternativeNewsDescription,
                            "analyzedAt" to Timestamp.now()
                        )
                    )
            } catch (e: Exception) {
                // Error guardando en Firestore, no crítico
            }
        }
    }

    fun resetState() {
        _uiState.value = MainUiState.Idle
    }
}
