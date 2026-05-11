package com.manuel.fakenewsdetector.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manuel.fakenewsdetector.domain.model.AnalysisResult
import com.manuel.fakenewsdetector.domain.model.NewsAnalysis
import com.manuel.fakenewsdetector.domain.model.Verdict
import com.manuel.fakenewsdetector.ui.components.AppBar
import com.manuel.fakenewsdetector.ui.components.HistoryListItem
import com.manuel.fakenewsdetector.ui.components.SectionHeader

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onNewsClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    // Datos de ejemplo
    val historyItems = remember {
        listOf(
            NewsAnalysis(
                id = "1",
                headline = "Científicos descubren cura para el cáncer",
                url = "https://example.com/noticia1",
                result = AnalysisResult(
                    verdict = Verdict.FALSA,
                    confidenceScore = 0.92,
                    explanation = "Noticia sin fuentes verificables"
                ),
                analyzedAt = System.currentTimeMillis() - 3600000
            ),
            NewsAnalysis(
                id = "2",
                headline = "Nueva ley de protección de datos entra en vigor",
                url = "https://example.com/noticia2",
                result = AnalysisResult(
                    verdict = Verdict.FIABLE,
                    confidenceScore = 0.88,
                    explanation = "Confirmada por fuentes oficiales"
                ),
                analyzedAt = System.currentTimeMillis() - 86400000
            ),
            NewsAnalysis(
                id = "3",
                headline = "Descubrimiento arqueológico en Egipto",
                url = "https://example.com/noticia3",
                result = AnalysisResult(
                    verdict = Verdict.DUDOSA,
                    confidenceScore = 0.65,
                    explanation = "Fuentes no verificadas"
                ),
                analyzedAt = System.currentTimeMillis() - 172800000
            ),
            NewsAnalysis(
                id = "4",
                headline = "Resultados de las elecciones locales",
                url = "https://example.com/noticia4",
                result = AnalysisResult(
                    verdict = Verdict.FIABLE,
                    confidenceScore = 0.95,
                    explanation = "Datos oficiales del ministerio"
                ),
                analyzedAt = System.currentTimeMillis() - 259200000
            ),
            NewsAnalysis(
                id = "5",
                headline = "Nuevo smartphone revolucionario",
                url = "https://example.com/noticia5",
                result = AnalysisResult(
                    verdict = Verdict.FALSA,
                    confidenceScore = 0.78,
                    explanation = "Especificaciones exageradas"
                ),
                analyzedAt = System.currentTimeMillis() - 345600000
            )
        )
    }

    val filteredItems = historyItems.filter {
        it.headline?.contains(searchQuery, ignoreCase = true) == true ||
                searchQuery.isEmpty()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = "Historial",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar en historial...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            // History list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    SectionHeader(
                        title = "${filteredItems.size} análisis encontrados",
                        action = {
                            IconButton(onClick = { /* Clear history */ }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Borrar historial",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }

                items(filteredItems) { news ->
                    HistoryListItem(
                        news = news,
                        onClick = { onNewsClick(news.id) }
                    )
                }
            }
        }
    }
}
