package com.manuel.fakenewsdetector.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.manuel.fakenewsdetector.domain.model.NewsAnalysis
import com.manuel.fakenewsdetector.domain.model.Verdict
import com.manuel.fakenewsdetector.ui.components.AppBar
import com.manuel.fakenewsdetector.ui.components.BottomNavBar
import com.manuel.fakenewsdetector.ui.components.ConfidenceBar
import com.manuel.fakenewsdetector.ui.components.NavItem
import com.manuel.fakenewsdetector.ui.components.NewsCard
import com.manuel.fakenewsdetector.ui.components.PrimaryButton
import com.manuel.fakenewsdetector.ui.components.SecondaryButton
import com.manuel.fakenewsdetector.ui.components.SectionLabel
import com.manuel.fakenewsdetector.ui.components.StatCardCompact
import com.manuel.fakenewsdetector.ui.components.VerdictBadge
import com.manuel.fakenewsdetector.ui.screens.history.HistoryViewModel

@Composable
fun MainScreen(
    isAdmin: Boolean,
    onAnalyzeClick: () -> Unit,
    onNewsClick: (String) -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    var selectedRoute by remember { mutableStateOf("home") }
    var inputText by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val historyViewModel: HistoryViewModel = viewModel()

    // 4 tabs en modo admin, 3 tabs en modo normal
    val navItems = if (isAdmin) {
        listOf(
            NavItem("Inicio", Icons.Default.Home, "home"),
            NavItem("Historial", Icons.AutoMirrored.Filled.List, "history"),
            NavItem("Admin", Icons.Default.Settings, "admin/stats"),
            NavItem("Perfil", Icons.Default.Person, "profile")
        )
    } else {
        listOf(
            NavItem("Inicio", Icons.Default.Home, "home"),
            NavItem("Historial", Icons.AutoMirrored.Filled.List, "history"),
            NavItem("Perfil", Icons.Default.Person, "profile")
        )
    }

    // Cargar análisis recientes desde Firestore
    val recentAnalyses by historyViewModel.historyItems.collectAsState()

    // Show error in snackbar
    LaunchedEffect(uiState) {
        if (uiState is MainUiState.Error) {
            snackbarHostState.showSnackbar((uiState as MainUiState.Error).message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppBar(title = "Fake News Detector")
        },
        bottomBar = {
            BottomNavBar(
                items = navItems,
                selectedRoute = selectedRoute,
                onItemSelected = { route ->
                    selectedRoute = route
                    onNavigate(route)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAnalyzeClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Analizar noticia")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Input Section
            SectionLabel(text = "Analizar Contenido de Noticia")

            // Input field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Pega aquí el texto completo de la noticia para analizar...") },
                label = { Text("Texto de la noticia") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 6,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            // Analyze button (disabled if empty)
            PrimaryButton(
                text = "Analizar noticia",
                onClick = { viewModel.analyze(inputText) },
                enabled = inputText.isNotBlank() && uiState !is MainUiState.Loading
            )

            // Loading state
            AnimatedVisibility(
                visible = uiState is MainUiState.Loading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analizando noticia...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Success state - Result card
            AnimatedVisibility(
                visible = uiState is MainUiState.Success,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val result = (uiState as? MainUiState.Success)?.result
                result?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Verdict badge and percentage
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                VerdictBadge(verdict = it.verdict)
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                Text(
                                    text = "${(it.confidenceScore * 100).toInt()}%",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = when (it.verdict) {
                                        Verdict.FIABLE -> com.manuel.fakenewsdetector.ui.theme.VerdictSuccess
                                        Verdict.DUDOSA -> com.manuel.fakenewsdetector.ui.theme.VerdictWarning
                                        Verdict.FALSA -> com.manuel.fakenewsdetector.ui.theme.VerdictDanger
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Confidence bar
                            ConfidenceBar(
                                confidence = it.confidenceScore,
                                verdict = it.verdict,
                                showLabel = false
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Explanation
                            Text(
                                text = it.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Sources
                            if (it.similarReliableArticles.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Fuentes verificadas:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                it.similarReliableArticles.forEach { source ->
                                    Text(
                                        text = "• $source",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Alternative news section
                            if (it.alternativeNewsUrl.isNotBlank()) {
                                Text(
                                    text = "Noticia alternativa verificada:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = it.alternativeNewsTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                )
                                if (it.alternativeNewsDescription.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = it.alternativeNewsDescription,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                val context = androidx.compose.ui.platform.LocalContext.current
                                SecondaryButton(
                                    text = "Ver noticia alternativa",
                                    onClick = {
                                        val intent = android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse(it.alternativeNewsUrl)
                                        )
                                        context.startActivity(intent)
                                    }
                                )
                            } else {
                                SecondaryButton(
                                    text = "Ver noticia alternativa",
                                    onClick = { },
                                    enabled = false
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Stats section
            SectionLabel(text = "Resumen")

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCardCompact(
                    title = "Análisis este mes",
                    value = recentAnalyses.size.toString(),
                    icon = Icons.Default.CheckCircle
                )

                StatCardCompact(
                    title = "Noticias falsas detectadas",
                    value = recentAnalyses.count { it.result?.verdict == Verdict.FALSA }.toString(),
                    icon = Icons.Default.CheckCircle
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel(text = "Análisis recientes")

            // Recent analyses
            recentAnalyses.forEach { news ->
                NewsCard(
                    news = news,
                    onClick = { onNewsClick(news.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
