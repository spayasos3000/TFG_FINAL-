package com.manuel.fakenewsdetector.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manuel.fakenewsdetector.domain.model.AnalysisResult
import com.manuel.fakenewsdetector.domain.model.NewsAnalysis
import com.manuel.fakenewsdetector.domain.model.User
import com.manuel.fakenewsdetector.domain.model.Verdict
import com.manuel.fakenewsdetector.ui.components.AppBar
import com.manuel.fakenewsdetector.ui.components.SectionHeader
import com.manuel.fakenewsdetector.ui.components.VerdictBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHistoryScreen(
    onBackClick: () -> Unit,
    onAnalysisClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }
    var showFilterMenu by remember { mutableStateOf(false) }

    val filters = listOf("Todos", "Fiables", "Dudosas", "Falsas", "Hoy", "Esta semana")

    // Datos de ejemplo
    val analyses = remember {
        listOf(
            NewsAnalysis(
                id = "1",
                userId = "user1",
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
                userId = "user2",
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
                userId = "user1",
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
                userId = "user3",
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
                userId = "user2",
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

    val users = remember {
        mapOf(
            "user1" to User(id = "user1", email = "juan@example.com", displayName = "Juan Pérez"),
            "user2" to User(id = "user2", email = "maria@example.com", displayName = "María García"),
            "user3" to User(id = "user3", email = "carlos@example.com", displayName = "Carlos López")
        )
    }

    val filteredAnalyses = analyses.filter { analysis ->
        val matchesSearch = analysis.headline?.contains(searchQuery, ignoreCase = true) == true ||
                searchQuery.isEmpty()

        val matchesFilter = when (selectedFilter) {
            "Fiables" -> analysis.result?.verdict == Verdict.FIABLE
            "Dudosas" -> analysis.result?.verdict == Verdict.DUDOSA
            "Falsas" -> analysis.result?.verdict == Verdict.FALSA
            "Hoy" -> System.currentTimeMillis() - analysis.analyzedAt < 86400000
            "Esta semana" -> System.currentTimeMillis() - analysis.analyzedAt < 604800000
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = "Historial Global",
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
                label = { Text("Buscar análisis...") },
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

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = showFilterMenu,
                    onExpandedChange = { showFilterMenu = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "Filtro: $selectedFilter",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFilterMenu) },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        filters.forEach { filter ->
                            DropdownMenuItem(
                                text = { Text(filter) },
                                onClick = {
                                    selectedFilter = filter
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            // Stats summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = filteredAnalyses.size.toString(), label = "Total")
                StatItem(
                    value = filteredAnalyses.count { it.result?.verdict == Verdict.FALSA }.toString(),
                    label = "Falsas"
                )
                StatItem(
                    value = filteredAnalyses.count { it.result?.verdict == Verdict.FIABLE }.toString(),
                    label = "Fiables"
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredAnalyses) { analysis ->
                    AdminAnalysisItem(
                        analysis = analysis,
                        user = analysis.userId?.let { users[it] },
                        onClick = { onAnalysisClick(analysis.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AdminAnalysisItem(
    analysis: NewsAnalysis,
    user: User?,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(analysis.analyzedAt))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            analysis.result?.let { result ->
                VerdictBadge(verdict = result.verdict)
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.padding(vertical = 4.dp))

        Text(
            text = analysis.headline ?: "Sin título",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (user != null) {
            Spacer(modifier = Modifier.padding(vertical = 2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = user.displayName ?: user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}
