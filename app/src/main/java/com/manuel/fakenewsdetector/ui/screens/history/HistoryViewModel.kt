package com.manuel.fakenewsdetector.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.manuel.fakenewsdetector.domain.model.AnalysisResult
import com.manuel.fakenewsdetector.domain.model.NewsAnalysis
import com.manuel.fakenewsdetector.domain.model.Verdict
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class HistoryUiState {
    data object Loading : HistoryUiState()
    data object Success : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

class HistoryViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null
    
    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    private val _historyItems = MutableStateFlow<List<NewsAnalysis>>(emptyList())
    val historyItems: StateFlow<List<NewsAnalysis>> = _historyItems.asStateFlow()
    
    init {
        loadHistory()
    }
    
    private fun loadHistory() {
        val currentUser = auth.currentUser ?: run {
            _uiState.value = HistoryUiState.Error("Usuario no autenticado")
            return
        }
        
        listenerRegistration = firestore
            .collection("users")
            .document(currentUser.uid)
            .collection("history")
            .orderBy("analyzedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    _uiState.value = HistoryUiState.Error(exception.message ?: "Error al cargar historial")
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        try {
                            NewsAnalysis(
                                id = doc.id,
                                headline = doc.getString("headline"),
                                url = doc.getString("url"),
                                result = AnalysisResult(
                                    verdict = when (doc.getString("verdict")) {
                                        "Fiable" -> Verdict.FIABLE
                                        "Dudosa" -> Verdict.DUDOSA
                                        "Falsa" -> Verdict.FALSA
                                        else -> Verdict.DUDOSA
                                    },
                                    confidenceScore = (doc.getDouble("confidenceScore")?.toFloat() ?: 0f).toDouble(),
                                    explanation = doc.getString("explanation") ?: "",
                                    detectedPatterns = (doc.get("detectedPatterns") as? List<String>) ?: emptyList(),
                                    sourcesChecked = (doc.get("sourcesChecked") as? List<String>)?.size ?: 0,
                                    similarReliableArticles = (doc.get("similarReliableArticles") as? List<String>) ?: emptyList(),
                                    blacklistedDomainMatch = doc.getBoolean("blacklistedDomainMatch") ?: false,
                                    alternativeNewsTitle = doc.getString("alternativeNewsTitle") ?: "",
                                    alternativeNewsUrl = doc.getString("alternativeNewsUrl") ?: "",
                                    alternativeNewsDescription = doc.getString("alternativeNewsDescription") ?: ""
                                ),
                                analyzedAt = doc.getTimestamp("analyzedAt")?.seconds?.times(1000L) ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _historyItems.value = items
                    _uiState.value = HistoryUiState.Success
                }
            }
    }
    
    fun deleteAnalysis(analysisId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = auth.currentUser ?: run {
            onError("Usuario no autenticado")
            return
        }
        
        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(currentUser.uid)
                    .collection("history")
                    .document(analysisId)
                    .delete()
                    .await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al eliminar análisis")
            }
        }
    }
    
    fun clearHistory(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = auth.currentUser ?: run {
            onError("Usuario no autenticado")
            return
        }
        
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .document(currentUser.uid)
                    .collection("history")
                    .get()
                    .await()
                
                snapshot.documents.forEach { doc ->
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .collection("history")
                        .document(doc.id)
                        .delete()
                        .await()
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al borrar historial")
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
