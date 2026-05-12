package com.manuel.fakenewsdetector.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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

sealed class UserLoadState {
    data object Loading : UserLoadState()
    data class Loaded(val userData: UserData) : UserLoadState()
    data object NotLoggedIn : UserLoadState()
}

data class UserData(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val role: String = "user",
    val createdAt: Timestamp? = null
)

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()
    
    private val _userLoadState = MutableStateFlow<UserLoadState>(UserLoadState.NotLoggedIn)
    val userLoadState: StateFlow<UserLoadState> = _userLoadState.asStateFlow()

    init {
        // Cargar datos del usuario si ya está logueado
        auth.currentUser?.let { 
            android.util.Log.d("AuthViewModel", "Usuario ya logueado, cargando datos: ${it.email}")
            loadUserIfNeeded(it.uid) 
        }
    }

    private fun loadUserIfNeeded(uid: String) {
        // ✅ Solo carga si no tenemos datos ya
        if (_userData.value != null) {
            android.util.Log.d("AuthViewModel", "Usuario ya cacheado, evitando recarga: $uid")
            return
        }
        
        android.util.Log.d("AuthViewModel", "Cargando usuario por primera vez: $uid")
        _userLoadState.value = UserLoadState.Loading
        loadUserData(uid)
    }

    private fun loadUserData(uid: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("AuthViewModel", "Intentando cargar datos del usuario $uid desde Firestore")
                
                // Verificar si Firebase está disponible antes de intentar
                if (!com.manuel.fakenewsdetector.utils.GoogleServicesChecker.isFirebaseAvailable()) {
                    android.util.Log.w("AuthViewModel", "Firebase no disponible, usando datos básicos")
                    // Fallback: usar datos básicos de Firebase Auth
                    loadUserDataFromAuth(uid)
                    return@launch
                }
                
                val document = firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()

                if (document.exists()) {
                    val role = document.getString("role") ?: "user"
                    android.util.Log.d("AuthViewModel", "Datos cargados desde Firestore, role: $role")
                    
                    val userData = UserData(
                        uid = document.getString("uid") ?: uid,
                        email = document.getString("email") ?: "",
                        displayName = document.getString("displayName") ?: "",
                        photoUrl = document.getString("photoUrl"),
                        role = role,
                        createdAt = document.getTimestamp("createdAt")
                    )
                    
                    _userData.value = userData
                    _userLoadState.value = UserLoadState.Loaded(userData)
                } else {
                    android.util.Log.w("AuthViewModel", "Usuario no encontrado en Firestore, usando datos de Auth")
                    loadUserDataFromAuth(uid)
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error cargando datos desde Firestore", e)
                // Si falla la carga desde Firestore, usar datos de Firebase Auth
                loadUserDataFromAuth(uid)
            }
        }
    }
    
    private fun loadUserDataFromAuth(uid: String) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                if (user != null) {
                    android.util.Log.d("AuthViewModel", "Cargando datos desde Firebase Auth para ${user.email}")
                    
                    // Lista de emails de admin para fallback cuando Firebase no está disponible
                    val adminEmails = setOf(
                        "admin@example.com",
                        "admin@fakenewsdetector.com",
                        "test@admin.com"
                    )
                    
                    val isAdmin = user.email?.lowercase() in adminEmails.map { it.lowercase() }
                    val role = if (isAdmin) "admin" else "user"
                    
                    if (isAdmin) {
                        android.util.Log.w("AuthViewModel", "Usuario detectado como admin por email: ${user.email}")
                    }
                    
                    val userData = UserData(
                        uid = uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: "",
                        photoUrl = user.photoUrl?.toString(),
                        role = role,
                        createdAt = null
                    )
                    
                    _userData.value = userData
                    _userLoadState.value = UserLoadState.Loaded(userData)
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error cargando datos desde Auth", e)
                _userData.value = null
                _userLoadState.value = UserLoadState.NotLoggedIn
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                android.util.Log.d("AuthViewModel", "Iniciando login para $email")
                auth.signInWithEmailAndPassword(email, password).await()
                val user = auth.currentUser
                _currentUser.value = user
                
                if (user != null) {
                    android.util.Log.d("AuthViewModel", "Login exitoso, cargando datos del usuario")
                    user.uid.let { 
                        loadUserData(it)
                        // Esperar un momento para que los datos se carguen antes de marcar como éxito
                        kotlinx.coroutines.delay(500)
                    }
                }
                
                _uiState.value = AuthUiState.Success
                android.util.Log.d("AuthViewModel", "Login completado exitosamente")
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error en login", e)
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

    fun signInWithGoogleCredential(credential: AuthCredential) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            try {
                android.util.Log.d("AuthViewModel", "Procesando credencial de Google")
                
                // Autenticar con Firebase usando la credencial de Google
                val result = auth.signInWithCredential(credential).await()
                val user = result.user
                
                _currentUser.value = user
                
                if (user != null) {
                    android.util.Log.d("AuthViewModel", "Login con Google exitoso para: ${user.email}")
                    
                    // Cargar datos del usuario
                    loadUserData(user.uid)
                    
                    // Esperar un momento para que los datos se carguen
                    kotlinx.coroutines.delay(500)
                }
                
                _uiState.value = AuthUiState.Success
                android.util.Log.d("AuthViewModel", "Login con Google completado exitosamente")
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error en login con Google", e)
                _uiState.value = AuthUiState.Error(
                    e.message ?: "Error al iniciar sesión con Google"
                )
            }
        }
    }

    fun loginWithGoogle(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            try {
                // TODO: Implementar Google Sign-In completo con Firebase UI Auth
                // Por ahora, mantener el placeholder hasta tener todo configurado
                _uiState.value = AuthUiState.Error("Google Sign-In en desarrollo - próximamente disponible")
                android.util.Log.d("AuthViewModel", "Google Sign-In solicitado pero no implementado completamente")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    e.message ?: "Error al iniciar sesión con Google"
                )
                android.util.Log.e("AuthViewModel", "Error en Google Sign-In", e)
            }
        }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _userData.value = null
        _uiState.value = AuthUiState.Idle
    }

    fun deleteAccount(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                if (user == null) {
                    onError("No hay usuario logueado")
                    return@launch
                }

                android.util.Log.d("AuthViewModel", "Iniciando eliminación de cuenta para ${user.email}")

                // Verificar si Firebase está disponible antes de intentar
                if (!com.manuel.fakenewsdetector.utils.GoogleServicesChecker.isFirebaseAvailable()) {
                    android.util.Log.w("AuthViewModel", "Firebase no disponible, intentando eliminar solo Auth")
                    // Intentar eliminar solo de Auth
                    try {
                        user.delete().await()
                        _currentUser.value = null
                        _userData.value = null
                        _uiState.value = AuthUiState.Idle
                        android.util.Log.d("AuthViewModel", "Cuenta eliminada solo de Auth")
                        onSuccess()
                    } catch (e: Exception) {
                        android.util.Log.e("AuthViewModel", "Error eliminando cuenta de Auth", e)
                        onError(e.message ?: "Error al eliminar cuenta")
                    }
                    return@launch
                }

                // Eliminar documento de Firestore
                val uid = user.uid
                firestore.collection("users")
                    .document(uid)
                    .delete()
                    .await()

                android.util.Log.d("AuthViewModel", "Documento de Firestore eliminado")

                // Eliminar usuario de Firebase Auth
                user.delete().await()

                android.util.Log.d("AuthViewModel", "Usuario eliminado de Firebase Auth")

                // Limpiar estado local
                _currentUser.value = null
                _userData.value = null
                _uiState.value = AuthUiState.Idle

                android.util.Log.d("AuthViewModel", "Cuenta eliminada exitosamente")
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error eliminando cuenta", e)
                onError(e.message ?: "Error al eliminar cuenta")
            }
        }
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

    fun searchUsers(query: String, onResult: (List<UserData>) -> Unit) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .whereGreaterThanOrEqualTo("email", query.lowercase())
                    .whereLessThanOrEqualTo("email", query.lowercase() + "\uf8ff")
                    .get()
                    .await()
                
                val users = snapshot.documents.mapNotNull { doc ->
                    UserData(
                        uid = doc.getString("uid") ?: doc.id,
                        email = doc.getString("email") ?: "",
                        displayName = doc.getString("displayName") ?: "",
                        photoUrl = doc.getString("photoUrl"),
                        role = doc.getString("role") ?: "user",
                        createdAt = doc.getTimestamp("createdAt")
                    )
                }
                onResult(users)
            } catch (e: Exception) {
                onResult(emptyList())
            }
        }
    }

    fun changeUserRole(uid: String, newRole: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(uid)
                    .update("role", newRole)
                    .await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al cambiar rol")
            }
        }
    }

    fun createAdminUser(
        email: String,
        password: String,
        displayName: String = "Admin",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("AuthViewModel", "Creando usuario admin: $email")
                
                // Crear usuario en Firebase Auth
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("No se pudo crear el usuario")
                
                android.util.Log.d("AuthViewModel", "Usuario creado en Auth, UID: $uid")
                
                // Guardar en Firestore con role "admin"
                val userData = hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "displayName" to displayName,
                    "role" to "admin",
                    "createdAt" to Timestamp.now()
                )
                
                // Verificar si Firebase está disponible
                if (!com.manuel.fakenewsdetector.utils.GoogleServicesChecker.isFirebaseAvailable()) {
                    android.util.Log.w("AuthViewModel", "Firebase no disponible, pero actualizando userData localmente")
                    // Actualizar userData localmente aunque no se guarde en Firestore
                    _currentUser.value = auth.currentUser
                    _userData.value = UserData(
                        uid = uid,
                        email = email,
                        displayName = displayName,
                        role = "admin",
                        createdAt = Timestamp.now()
                    )
                    onSuccess()
                    return@launch
                }
                
                firestore.collection("users")
                    .document(uid)
                    .set(userData)
                    .await()
                
                android.util.Log.d("AuthViewModel", "Usuario admin guardado en Firestore")
                
                // Actualizar userData inmediatamente
                _currentUser.value = auth.currentUser
                _userData.value = UserData(
                    uid = uid,
                    email = email,
                    displayName = displayName,
                    role = "admin",
                    createdAt = Timestamp.now()
                )
                
                android.util.Log.d("AuthViewModel", "Usuario admin creado exitosamente")
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error creando usuario admin", e)
                onError(e.message ?: "Error al crear usuario admin")
            }
        }
    }

    fun updateUserProfile(
        displayName: String,
        photoUrl: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: throw Exception("No hay usuario logueado")
                
                // Actualizar en Firebase Auth
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(photoUrl?.let { android.net.Uri.parse(it) })
                    .build()
                
                currentUser.updateProfile(profileUpdates).await()
                
                // Actualizar en Firestore
                val updates = hashMapOf<String, Any>(
                    "displayName" to displayName
                )
                
                photoUrl?.let {
                    updates["photoUrl"] = it
                }
                
                firestore.collection("users")
                    .document(currentUser.uid)
                    .update(updates)
                    .await()
                
                // Recargar datos del usuario
                loadUserData(currentUser.uid)
                
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al actualizar perfil")
            }
        }
    }
}
