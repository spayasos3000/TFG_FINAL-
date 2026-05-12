package com.manuel.fakenewsdetector.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.manuel.fakenewsdetector.domain.model.AnalysisResult
import com.manuel.fakenewsdetector.domain.model.NewsAnalysis
import com.manuel.fakenewsdetector.domain.model.Verdict
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class DetailUiState {
    data object Loading : DetailUiState()
    data class Success(val analysis: NewsAnalysis) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

class AnalysisDetailViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()
    
    fun loadAnalysis(analysisId: String) {
        val currentUser = auth.currentUser ?: run {
            _uiState.value = DetailUiState.Error("Usuario no autenticado")
            return
        }
        
        viewModelScope.launch {
            try {
                val document = firestore.collection("users")
                    .document(currentUser.uid)
                    .collection("history")
                    .document(analysisId)
                    .get()
                    .await()
                
                if (document.exists()) {
                    val analysis = NewsAnalysis(
                        id = document.id,
                        headline = document.getString("headline"),
                        url = document.getString("url"),
                        content = document.getString("content"),
                        result = AnalysisResult(
                            verdict = when (document.getString("verdict")) {
                                "Fiable" -> Verdict.FIABLE
                                "Dudosa" -> Verdict.DUDOSA
                                "Falsa" -> Verdict.FALSA
                                else -> Verdict.DUDOSA
                            },
                            confidenceScore = (document.getDouble("confidenceScore")?.toFloat() ?: 0f).toDouble(),
                            explanation = document.getString("explanation") ?: "",
                            detectedPatterns = document.get("detectedPatterns") as? List<String> ?: emptyList(),
                            sourcesChecked = (document.get("sourcesChecked") as? List<String>)?.size ?: 0,
                            similarReliableArticles = document.get("similarReliableArticles") as? List<String> ?: emptyList(),
                            blacklistedDomainMatch = document.getBoolean("blacklistedDomainMatch") ?: false,
                            alternativeNewsTitle = document.getString("alternativeNewsTitle") ?: "",
                            alternativeNewsUrl = document.getString("alternativeNewsUrl") ?: "",
                            alternativeNewsDescription = document.getString("alternativeNewsDescription") ?: ""
                        ),
                        analyzedAt = document.getTimestamp("analyzedAt")?.seconds?.times(1000L) ?: System.currentTimeMillis()
                    )
                    _uiState.value = DetailUiState.Success(analysis)
                } else {
                    _uiState.value = DetailUiState.Error("Análisis no encontrado")
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message ?: "Error al cargar análisis")
            }
        }
    }
}
