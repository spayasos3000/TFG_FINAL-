package com.manuel.fakenewsdetector.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manuel.fakenewsdetector.domain.model.BlacklistedDomain
import com.manuel.fakenewsdetector.ui.components.AppBar
import com.manuel.fakenewsdetector.ui.components.SectionHeader
import com.manuel.fakenewsdetector.ui.components.StatCard
import com.manuel.fakenewsdetector.ui.theme.VerdictDanger
import com.manuel.fakenewsdetector.ui.theme.VerdictSuccess
import com.manuel.fakenewsdetector.ui.theme.VerdictWarning

@Composable
fun AdminStatsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Datos de ejemplo
    val stats = remember {
        mapOf(
            "totalAnalyses" to 12547,
            "totalUsers" to 3420,
            "fakeNewsDetected" to 3892,
            "dubiousNews" to 2156,
            "reliableNews" to 6499,
            "blacklistedDomains" to 156,
            "todayAnalyses" to 342
        )
    }

    val recentActivity = remember {
        listOf(
            "Usuario nuevo registrado: user1234" to System.currentTimeMillis() - 300000,
            "Dominio añadido a lista negra: fakeweb.com" to System.currentTimeMillis() - 600000,
            "Análisis masivo completado: 500 noticias" to System.currentTimeMillis() - 1200000,
            "Alerta: Aumento de noticias falsas en sector político" to System.currentTimeMillis() - 1800000
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = "Panel de Administración",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overview stats
            item {
                Text(
                    text = "Resumen del Sistema",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Key metrics cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Análisis",
                        value = stats["totalAnalyses"].toString(),
                        icon = Icons.Default.Settings,
                        trend = "+${stats["todayAnalyses"]} hoy",
                        trendPositive = true,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Usuarios",
                        value = stats["totalUsers"].toString(),
                        icon = Icons.Default.Person,
                        trend = "Activos",
                        trendPositive = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Verdict distribution
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Distribución de Veredictos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VerdictStatCard(
                        label = "Fiables",
                        count = stats["reliableNews"] ?: 0,
                        total = stats["totalAnalyses"] ?: 1,
                        color = VerdictSuccess
                    )

                    VerdictStatCard(
                        label = "Dudosas",
                        count = stats["dubiousNews"] ?: 0,
                        total = stats["totalAnalyses"] ?: 1,
                        color = VerdictWarning
                    )

                    VerdictStatCard(
                        label = "Falsas",
                        count = stats["fakeNewsDetected"] ?: 0,
                        total = stats["totalAnalyses"] ?: 1,
                        color = VerdictDanger
                    )
                }
            }

            // Blacklist stats
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(modifier = Modifier.padding(horizontal = 12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dominios en Lista Negra",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${stats["blacklistedDomains"]} dominios bloqueados",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Gestionar",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Recent activity
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Actividad Reciente")
            }

            items(recentActivity) { (activity, timestamp) ->
                ActivityItem(activity = activity, timestamp = timestamp)
            }
        }
    }
}

@Composable
private fun VerdictStatCard(
    label: String,
    count: Int,
    total: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val percentage = if (total > 0) (count * 100 / total) else 0

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = color
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ActivityItem(
    activity: String,
    timestamp: Long
) {
    val timeAgo = when {
        System.currentTimeMillis() - timestamp < 60000 -> "hace un momento"
        System.currentTimeMillis() - timestamp < 3600000 -> "hace ${(System.currentTimeMillis() - timestamp) / 60000} min"
        else -> "hace ${(System.currentTimeMillis() - timestamp) / 3600000} h"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = activity,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = timeAgo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
