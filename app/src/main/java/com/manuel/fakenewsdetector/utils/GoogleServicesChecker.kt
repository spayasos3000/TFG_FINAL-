package com.manuel.fakenewsdetector.utils

import android.content.Context
import android.content.pm.PackageManager
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Utilidad para verificar la disponibilidad de Google Play Services y Firebase
 * Evita errores de SecurityException y problemas de memoria en emuladores
 */
object GoogleServicesChecker {
    
    private var _isAvailable: Boolean? = null
    private var _lastCheck: Long = 0
    private val CHECK_INTERVAL = 5000L // 5 segundos
    
    /**
     * Verifica si Google Play Services está disponible
     */
    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // Cache del resultado para evitar verificaciones repetidas
        if (_isAvailable != null && (currentTime - _lastCheck) < CHECK_INTERVAL) {
            return _isAvailable!!
        }
        
        _lastCheck = currentTime
        
        return try {
            val apiAvailability = GoogleApiAvailability.getInstance()
            val result = apiAvailability.isGooglePlayServicesAvailable(context)
            
            _isAvailable = result == com.google.android.gms.common.ConnectionResult.SUCCESS
            
            if (!_isAvailable!!) {
                android.util.Log.w("GoogleServicesChecker", 
                    "Google Play Services no disponible: $result")
            }
            
            _isAvailable!!
        } catch (e: Exception) {
            android.util.Log.e("GoogleServicesChecker", 
                "Error verificando Google Play Services", e)
            _isAvailable = false
            false
        }
    }
    
    /**
     * Verifica si Firebase está disponible y funcional
     */
    fun isFirebaseAvailable(): Boolean {
        return try {
            // Intentar acceder a Firestore para verificar disponibilidad
            Firebase.firestore
            true
        } catch (e: Exception) {
            android.util.Log.e("GoogleServicesChecker", 
                "Firebase no disponible", e)
            false
        }
    }
    
    /**
     * Verificación combinada de ambos servicios
     */
    fun areServicesAvailable(context: Context): Boolean {
        return isGooglePlayServicesAvailable(context) && isFirebaseAvailable()
    }
    
    /**
     * Limpia el cache para forzar nueva verificación
     */
    fun clearCache() {
        _isAvailable = null
        _lastCheck = 0
    }
    
    /**
     * Verifica si el dispositivo es un emulador
     */
    fun isEmulator(): Boolean {
        return try {
            val isEmulator = (android.os.Build.FINGERPRINT.startsWith("generic")
                    || android.os.Build.FINGERPRINT.startsWith("unknown")
                    || android.os.Build.MODEL.contains("google_sdk")
                    || android.os.Build.MODEL.contains("Emulator")
                    || android.os.Build.MODEL.contains("Android SDK built for x86")
                    || android.os.Build.MANUFACTURER.contains("Genymotion")
                    || (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                    || "google_sdk" == android.os.Build.PRODUCT)
            
            if (isEmulator) {
                android.util.Log.i("GoogleServicesChecker", "Detectado entorno de emulador")
            }
            
            isEmulator
        } catch (e: Exception) {
            android.util.Log.e("GoogleServicesChecker", "Error detectando emulador", e)
            false
        }
    }
}
