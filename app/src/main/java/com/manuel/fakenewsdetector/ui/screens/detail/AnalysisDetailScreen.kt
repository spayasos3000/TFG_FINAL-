package com.manuel.fakenewsdetector.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.manuel.fakenewsdetector.domain.model.NewsAnalysis
import com.manuel.fakenewsdetector.domain.model.Verdict
import com.manuel.fakenewsdetector.ui.components.AppBar
import com.manuel.fakenewsdetector.ui.components.ConfidenceBar
import com.manuel.fakenewsdetector.ui.components.PrimaryButton
import com.manuel.fakenewsdetector.ui.components.SectionLabel
import com.manuel.fakenewsdetector.ui.components.VerdictBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalysisDetailScreen(
    analysisId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnalysisDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(analysisId) {
        viewModel.loadAnalysis(analysisId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = "Detalle del Análisis",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBackClick,
                actions = {
                    val successState = uiState as? DetailUiState.Success
                    if (successState != null) {
                        IconButton(onClick = { /* Toggle favorite */ }) {
                            Icon(
                                imageVector = if (successState.analysis.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (successState.analysis.isFavorite) "Quitar favorito" else "Añadir favorito",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { /* Compartir */ }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when (uiState) {
            is DetailUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is DetailUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = (uiState as DetailUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is DetailUiState.Success -> {
                val analysis = (uiState as DetailUiState.Success).analysis
                var isFavorite by remember { mutableStateOf(analysis.isFavorite) }

                val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
                val formattedDate = dateFormat.format(Date(analysis.analyzedAt))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // URL
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = analysis.url ?: analysis.content ?: "URL no disponible",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    // Title
                    Text(
                        text = analysis.headline ?: "Sin título",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Date
                    Text(
                        text = "Analizado el $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    // Verdict section
                    analysis.result?.let { result ->
                SectionLabel(text = "Veredicto")

                Row(verticalAlignment = Alignment.CenterVertically) {
                    VerdictBadge(verdict = result.verdict)

                    Spacer(modifier = Modifier.width(16.dp))

                    ConfidenceBar(
                        confidence = result.confidenceScore,
                        verdict = result.verdict,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Explanation
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (result.verdict) {
                        Verdict.FIABLE -> MaterialTheme.colorScheme.tertiaryContainer
                        Verdict.DUDOSA -> MaterialTheme.colorScheme.secondaryContainer
                        Verdict.FALSA -> MaterialTheme.colorScheme.errorContainer
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = result.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (result.verdict) {
                            Verdict.FIABLE -> MaterialTheme.colorScheme.onTertiaryContainer
                            Verdict.DUDOSA -> MaterialTheme.colorScheme.onSecondaryContainer
                            Verdict.FALSA -> MaterialTheme.colorScheme.onErrorContainer
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Detected patterns
                if (result.detectedPatterns.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionLabel(text = "Patrones detectados")

                    result.detectedPatterns.forEach { pattern ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = pattern,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Stats
                Spacer(modifier = Modifier.height(16.dp))
                SectionLabel(text = "Estadísticas del análisis")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Fuentes verificadas",
                        value = result.sourcesChecked.toString()
                    )
                    StatItem(
                        label = "Dominio en lista negra",
                        value = if (result.blacklistedDomainMatch) "Sí" else "No"
                    )
                    StatItem(
                        label = "Artículos similares",
                        value = result.similarReliableArticles.size.toString()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actions
            PrimaryButton(
                text = "Analizar otra noticia",
                onClick = onBackClick
            )
        }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
