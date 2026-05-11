package com.manuel.fakenewsdetector.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

data class UserData(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val role: String = "user",
    val createdAt: Timestamp? = null
)

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    init {
        // Cargar datos del usuario si ya está logueado
        auth.currentUser?.let { loadUserData(it.uid) }
    }

    private fun loadUserData(uid: String) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()

                if (document.exists()) {
                    _userData.value = UserData(
                        uid = document.getString("uid") ?: "",
                        email = document.getString("email") ?: "",
                        displayName = document.getString("displayName") ?: "",
                        photoUrl = document.getString("photoUrl"),
                        role = document.getString("role") ?: "user",
                        createdAt = document.getTimestamp("createdAt")
                    )
                }
            } catch (e: Exception) {
                // Si falla la carga desde Firestore, usar datos de Firebase Auth
                _userData.value = null
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                auth.signInWithEmailAndPassword(email, password).await()
                val user = auth.currentUser
                _currentUser.value = user
                user?.uid?.let { loadUserData(it) }
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    e.message ?: "Error al iniciar sesión"
                )
            }
        }
    }

    fun register(email: String, password: String, displayName: String? = null) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("No se pudo crear el usuario")
                val finalDisplayName = displayName ?: email.substringBefore("@")

                // Guardar datos en Firestore
                val userData = hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "displayName" to finalDisplayName,
                    "createdAt" to Timestamp.now(),
                    "role" to "user"
                )

                firestore.collection("users")
                    .document(uid)
                    .set(userData)
                    .await()

                _currentUser.value = auth.currentUser
                _userData.value = UserData(
                    uid = uid,
                    email = email,
                    displayName = finalDisplayName,
                    role = "user",
                    createdAt = Timestamp.now()
                )
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    e.message ?: "Error al registrar usuario"
                )
            }
        }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _userData.value = null
        _uiState.value = AuthUiState.Idle
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun updatePhotoUrl(photoUrl: String) {
        viewModelScope.launch {
            try {
                val currentUid = auth.currentUser?.uid ?: return@launch
                firestore.collection("users")
                    .document(currentUid)
                    .update("photoUrl", photoUrl)
                    .await()

                // Update local state
                _userData.value = _userData.value?.copy(photoUrl = photoUrl)
            } catch (e: Exception) {
                // Silently fail - photo update is not critical
            }
        }
    }
}
