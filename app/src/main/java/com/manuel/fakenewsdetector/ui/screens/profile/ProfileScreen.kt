package com.manuel.fakenewsdetector.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.manuel.fakenewsdetector.ui.screens.login.AuthViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.manuel.fakenewsdetector.ui.components.AppBar
import com.manuel.fakenewsdetector.ui.components.SectionHeader
import com.manuel.fakenewsdetector.ui.components.SettingRow
import com.manuel.fakenewsdetector.ui.components.SettingRowSwitch
import com.manuel.fakenewsdetector.ui.components.StatCard

@Composable
fun ProfileScreen(
    isAdminMode: Boolean,
    onAdminModeToggle: (Boolean) -> Unit,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onNavigateToAdmin: (String) -> Unit,
    onBackClick: () -> Unit,
    onPhotoSelected: (android.net.Uri) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    var notificationsEnabled by remember { mutableStateOf(true) }

    // Obtener datos del usuario de Firebase/Firestore
    val userData by viewModel.userData.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val userName = userData?.displayName?.takeIf { it.isNotBlank() }
        ?: currentUser?.displayName
        ?: currentUser?.email?.substringBefore("@")
        ?: "Usuario"
    val userEmail = userData?.email ?: currentUser?.email ?: ""

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = "Perfil",
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onBackClick,
                actions = {
                    TextButton(onClick = onEditProfileClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with photo picker
                ProfilePhotoPicker(
                    photoUrl = userData?.photoUrl,
                    onPhotoSelected = onPhotoSelected
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }

            HorizontalDivider()

            // Stats section
            SectionHeader(title = "Estadísticas")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total de análisis",
                    value = "156",
                    icon = Icons.Default.Search,
                    trend = "+12 este mes",
                    trendPositive = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Noticias falsas",
                        value = "34",
                        icon = Icons.Default.Delete,
                        trend = "22% detectadas",
                        trendPositive = false,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Noticias fiables",
                        value = "98",
                        icon = Icons.Default.CheckCircle,
                        trend = "63% verificadas",
                        trendPositive = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Settings
            SectionHeader(title = "Ajustes")

            SettingRowSwitch(
                title = "Notificaciones",
                subtitle = "Recibir alertas sobre nuevos análisis",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it },
                icon = Icons.Default.Notifications
            )

            SettingRowSwitch(
                title = "Modo oscuro",
                subtitle = "Cambiar tema de la aplicación",
                checked = isDarkMode,
                onCheckedChange = onDarkModeToggle,
                icon = Icons.Default.Settings
            )

            SettingRow(
                title = "Privacidad y seguridad",
                subtitle = "Gestionar datos y permisos",
                onClick = { /* Navegar a privacidad */ },
                icon = Icons.Default.Lock
            )

            SettingRow(
                title = "Ayuda y soporte",
                subtitle = "Preguntas frecuentes y contacto",
                onClick = { /* Navegar a ayuda */ },
                icon = Icons.Default.Info
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Admin section
            SettingRowSwitch(
                title = "Modo Administrador",
                subtitle = "Activar funciones de administración",
                checked = isAdminMode,
                onCheckedChange = onAdminModeToggle,
                icon = Icons.Default.Lock
            )

            if (isAdminMode) {
                SectionHeader(title = "Administración")

                SettingRow(
                    title = "Estadísticas globales",
                    subtitle = "Ver métricas del sistema",
                    onClick = { onNavigateToAdmin("stats") },
                    icon = Icons.Default.Settings
                )

                SettingRow(
                    title = "Historial global",
                    subtitle = "Ver todos los análisis",
                    onClick = { onNavigateToAdmin("history") },
                    icon = Icons.Default.Search
                )

                SettingRow(
                    title = "Lista negra",
                    subtitle = "Gestionar dominios bloqueados",
                    onClick = { onNavigateToAdmin("blacklist") },
                    icon = Icons.Default.Lock
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Account actions
            SectionHeader(title = "Cuenta")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = onLogoutClick,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Cerrar sesión",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onDeleteAccountClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Eliminar cuenta",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App version
            Text(
                text = "Fake News Detector v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfilePhotoPicker(
    photoUrl: String?,
    onPhotoSelected: (android.net.Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { onPhotoSelected(it) }
    }

    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(20.dp)
                        .size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Camera icon overlay
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(32.dp)
                .clickable { imagePickerLauncher.launch("image/*") },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Cambiar foto",
                modifier = Modifier.padding(6.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
