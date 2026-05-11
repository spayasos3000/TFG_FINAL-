package com.manuel.fakenewsdetector.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.manuel.fakenewsdetector.data.repository.NewsRepository
import com.manuel.fakenewsdetector.domain.model.AnalysisResult
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

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Idle)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val repository = NewsRepository()
    
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
        
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("history")
            .add(
                mapOf(
                    "query" to input,
                    "result" to result.verdict.name.lowercase(),
                    "confidence" to result.confidenceScore,
                    "summary" to result.explanation,
                    "sources" to result.similarReliableArticles,
                    "alternativeNewsTitle" to result.alternativeNewsTitle,
                    "alternativeNewsUrl" to result.alternativeNewsUrl,
                    "alternativeNewsDescription" to result.alternativeNewsDescription,
                    "timestamp" to Timestamp.now()
                )
            )
    }

    fun resetState() {
        _uiState.value = MainUiState.Idle
    }
}
