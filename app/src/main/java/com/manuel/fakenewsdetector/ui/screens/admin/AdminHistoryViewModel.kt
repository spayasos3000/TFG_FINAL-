package com.manuel.fakenewsdetector.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.DocumentSnapshot
import com.manuel.fakenewsdetector.domain.model.NewsAnalysis
import com.manuel.fakenewsdetector.domain.model.AnalysisResult
import com.manuel.fakenewsdetector.domain.model.Verdict

sealed class AdminHistoryUiState {
    data object Loading : AdminHistoryUiState()
    data class Success(val analyses: List<NewsAnalysis>) : AdminHistoryUiState()
    data class Error(val message: String) : AdminHistoryUiState()
}

class AdminHistoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<AdminHistoryUiState>(AdminHistoryUiState.Loading)
    val uiState: StateFlow<AdminHistoryUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    
    private var searchQuery = ""
    private var filterVerdict = "Todos"

    fun loadGlobalHistory(
        query: String = "",
        verdictFilter: String = "Todos"
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = AdminHistoryUiState.Loading
                searchQuery = query
                filterVerdict = verdictFilter

                val allAnalyses = mutableListOf<NewsAnalysis>()
                val usersCollection = firestore.collection("users")

                // Obtener todos los usuarios y sus análisis
                val usersSnapshot = usersCollection.get().await()

                for (userDoc in usersSnapshot.documents) {
                    val historyCollection = userDoc.reference.collection("history")
                    var queryRef: Query = historyCollection.orderBy("analyzedAt", Query.Direction.DESCENDING)

                    // Aplicar filtro de veredicto
                    if (verdictFilter != "Todos") {
                        queryRef = queryRef.whereEqualTo("verdict", verdictFilter)
                    }

                    // Aplicar filtro de búsqueda
                    if (query.isNotEmpty()) {
                        queryRef = queryRef.whereGreaterThanOrEqualTo("headline", query)
                            .whereLessThanOrEqualTo("headline", query + "\uf8ff")
                    }

                    val historySnapshot = queryRef.get().await()

                    // Convertir documentos a NewsAnalysis
                    historySnapshot.documents.forEach { doc: DocumentSnapshot ->
                        try {
                            val analysis = NewsAnalysis(
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
                            allAnalyses.add(analysis)
                        } catch (e: Exception) {
                            // Ignorar documentos con errores
                        }
                    }
                }

                // Ordenar por fecha (más recientes primero)
                val sortedAnalyses = allAnalyses.sortedByDescending { it.analyzedAt }
                _uiState.value = AdminHistoryUiState.Success(sortedAnalyses)

            } catch (e: Exception) {
                _uiState.value = AdminHistoryUiState.Error(e.message ?: "Error al cargar historial global")
            }
        }
    }

    fun deleteAnalysis(userId: String, analysisId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("history")
                    .document(analysisId)
                    .delete()
                    .await()
                onSuccess()
                // Recargar datos
                loadGlobalHistory(searchQuery, filterVerdict)
            } catch (e: Exception) {
                onError(e.message ?: "Error al eliminar análisis")
            }
        }
    }
}
