package com.manuel.fakenewsdetector.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manuel.fakenewsdetector.domain.model.BlacklistedDomain
import com.manuel.fakenewsdetector.ui.components.AppBar
import com.manuel.fakenewsdetector.ui.components.SectionHeader
import com.manuel.fakenewsdetector.ui.theme.VerdictDanger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminBlacklistScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var domainToDelete by remember { mutableStateOf<BlacklistedDomain?>(null) }

    // Datos de ejemplo
    var blacklist by remember {
        mutableStateOf(
            listOf(
                BlacklistedDomain(
                    domain = "fakeweb.com",
                    reason = "Noticias falsas verificadas",
                    source = "Reporte de usuarios",
                    addedAt = System.currentTimeMillis() - 86400000 * 30
                ),
                BlacklistedDomain(
                    domain = "noticias-falsas.org",
                    reason = "Desinformación sistemática",
                    source = "Verificación manual",
                    addedAt = System.currentTimeMillis() - 86400000 * 15
                ),
                BlacklistedDomain(
                    domain = "clickbait-news.com",
                    reason = "Clickbait y sensacionalismo",
                    source = "Análisis automático",
                    addedAt = System.currentTimeMillis() - 86400000 * 7
                ),
                BlacklistedDomain(
                    domain = "conspiraciones.net",
                    reason = "Teorías conspirativas sin fundamento",
                    source = "Reporte de usuarios",
                    addedAt = System.currentTimeMillis() - 86400000 * 5
                ),
                BlacklistedDomain(
                    domain = "noticias-amarillistas.es",
                    reason = "Información engañosa",
                    source = "Verificación manual",
                    addedAt = System.currentTimeMillis() - 86400000 * 2
                )
            )
        )
    }

    val filteredBlacklist = blacklist.filter {
        it.domain.contains(searchQuery, ignoreCase = true) ||
                it.reason.contains(searchQuery, ignoreCase = true) ||
                searchQuery.isEmpty()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = "Lista Negra de Dominios",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir dominio")
            }
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
                label = { Text("Buscar dominio...") },
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

            // Stats
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = VerdictDanger.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = VerdictDanger,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "${blacklist.size} dominios bloqueados",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Las noticias de estos dominios se marcarán automáticamente como falsas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List header
            SectionHeader(title = "Dominios Bloqueados")

            // List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredBlacklist) { domain ->
                    BlacklistItem(
                        domain = domain,
                        onDelete = { domainToDelete = domain }
                    )
                }
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        AddDomainDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newDomain, reason, source ->
                blacklist = blacklist + BlacklistedDomain(
                    domain = newDomain,
                    reason = reason,
                    source = source
                )
                showAddDialog = false
            }
        )
    }

    // Delete confirmation dialog
    if (domainToDelete != null) {
        AlertDialog(
            onDismissRequest = { domainToDelete = null },
            title = { Text("Eliminar dominio") },
            text = { Text("¿Estás seguro de que quieres eliminar '${domainToDelete?.domain}' de la lista negra?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        blacklist = blacklist.filter { it.domain != domainToDelete?.domain }
                        domainToDelete = null
                    }
                ) {
                    Text("Eliminar", color = VerdictDanger)
                }
            },
            dismissButton = {
                TextButton(onClick = { domainToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun BlacklistItem(
    domain: BlacklistedDomain,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(domain.addedAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = domain.domain,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = domain.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Añadido: $formattedDate • Fuente: ${domain.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = VerdictDanger
                )
            }
        }
    }
}

@Composable
private fun AddDomainDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var domain by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Dominio a Lista Negra") },
        text = {
            Column {
                OutlinedTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = { Text("Dominio") },
                    placeholder = { Text("ejemplo.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Razón") },
                    placeholder = { Text("¿Por qué se bloquea?") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Fuente") },
                    placeholder = { Text("¿Quién lo reportó?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(domain, reason, source) },
                enabled = domain.isNotBlank() && reason.isNotBlank()
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
