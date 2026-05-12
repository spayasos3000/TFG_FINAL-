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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.manuel.fakenewsdetector.domain.model.User
import com.manuel.fakenewsdetector.ui.components.AppBar
import com.manuel.fakenewsdetector.ui.components.PrimaryButton
import com.manuel.fakenewsdetector.ui.components.SectionHeader
import com.manuel.fakenewsdetector.ui.components.SettingRow
import com.manuel.fakenewsdetector.ui.components.SettingRowSwitch
import com.manuel.fakenewsdetector.ui.components.StatCard
import com.manuel.fakenewsdetector.ui.screens.login.AuthViewModel
import com.manuel.fakenewsdetector.ui.screens.login.UserData

@Composable
fun ProfileScreen(
    userData: UserData?,
    onLogoutClick: () -> Unit,
    onNavigateToAdmin: (String) -> Unit,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onEditProfileClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onBackClick: () -> Unit,
    onPhotoSelected: (android.net.Uri) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userData?.displayName ?: "") }

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
                    TextButton(onClick = { showEditDialog = true }) {
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Admin section - solo visible si el usuario tiene role "admin" en Firestore
            val isAdmin = userData?.role == "admin"
            if (isAdmin) {
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
                    title = "Gestión de usuarios",
                    subtitle = "Administrar roles de usuarios",
                    onClick = { onNavigateToAdmin("users") },
                    icon = Icons.Default.Person
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
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar sesión")
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar cuenta")
                }
            }
        }
    }
    
    // Diálogo de edición de perfil
    EditProfileDialog(
        isVisible = showEditDialog,
        currentName = userName,
        onDismiss = { showEditDialog = false },
        onSave = { newName ->
            viewModel.updateUserProfile(
                displayName = newName,
                onSuccess = {
                    showEditDialog = false
                },
                onError = { error ->
                    // Manejar error (podrías mostrar un snackbar)
                    showEditDialog = false
                }
            )
        }
    )
    
    // Diálogo de confirmación de eliminación de cuenta
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Eliminar cuenta",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer y se eliminarán todos tus datos permanentemente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccountClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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

// Diálogo de edición de perfil
@Composable
fun EditProfileDialog(
    isVisible: Boolean,
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editedName by remember { mutableStateOf(currentName) }
    var isSaving by remember { mutableStateOf(false) }

    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Editar Perfil",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Actualiza tu información de perfil",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Nombre") },
                        placeholder = { Text("Ingresa tu nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedName.isNotBlank()) {
                            isSaving = true
                            onSave(editedName.trim())
                        }
                    },
                    enabled = editedName.isNotBlank() && !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Guardar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isSaving
                ) {
                    Text("Cancelar")
                }
            },
            modifier = modifier
        )
    }
}
