package com.manuel.fakenewsdetector.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.manuel.fakenewsdetector.MainActivity
import com.manuel.fakenewsdetector.R
import com.manuel.fakenewsdetector.domain.model.Verdict

class AppNotificationManager(private val context: Context) {
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    companion object {
        const val CHANNEL_ANALYSIS_ID = "analysis_updates"
        const val CHANNEL_ADMIN_ID = "admin_notifications"
        const val CHANNEL_SYSTEM_ID = "system_notifications"
        
        const val NOTIFICATION_ANALYSIS_COMPLETE = 1001
        const val NOTIFICATION_FAKE_NEWS = 1002
        const val NOTIFICATION_ADMIN_ALERT = 1003
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para análisis completados
            val analysisChannel = NotificationChannel(
                CHANNEL_ANALYSIS_ID,
                "Análisis de Noticias",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones sobre análisis de noticias completados"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
            }
            
            // Canal para alertas de administrador
            val adminChannel = NotificationChannel(
                CHANNEL_ADMIN_ID,
                "Alertas de Administrador",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones importantes para administradores"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            
            // Canal para notificaciones del sistema
            val systemChannel = NotificationChannel(
                CHANNEL_SYSTEM_ID,
                "Sistema",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones generales del sistema"
                enableLights(true)
            }
            
            notificationManager.createNotificationChannels(listOf(
                analysisChannel,
                adminChannel,
                systemChannel
            ))
        }
    }
    
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showAnalysisCompleteNotification(
        headline: String,
        verdict: Verdict,
        confidenceScore: Double
    ) {
        if (!hasNotificationPermission()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_analysis", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val (title, message, icon) = when (verdict) {
            Verdict.FIABLE -> Triple(
                "✅ Noticia Verificada",
                "La noticia \"$headline\" ha sido verificada como fiable (${(confidenceScore * 100).toInt()}%)",
                R.drawable.ic_check_circle
            )
            Verdict.DUDOSA -> Triple(
                "⚠️ Noticia Dudosa",
                "La noticia \"$headline\" requiere más verificación (${(confidenceScore * 100).toInt()}%)",
                R.drawable.ic_warning
            )
            Verdict.FALSA -> Triple(
                "🚨 Noticia Falsa Detectada",
                "¡Alerta! La noticia \"$headline\" ha sido identificada como falsa (${(confidenceScore * 100).toInt()}%)",
                R.drawable.ic_error
            )
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ANALYSIS_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ANALYSIS_COMPLETE, notification)
    }
    
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showFakeNewsAlert(headline: String, explanation: String) {
        if (!hasNotificationPermission()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("alert_fake_news", headline)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ANALYSIS_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("🚨 Alerta de Noticia Falsa")
            .setContentText("Se detectó una noticia falsa: \"$headline\"")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Se detectó una noticia falsa: \"$headline\"\n\n$explanation"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()
        
        notificationManager.notify(NOTIFICATION_FAKE_NEWS, notification)
    }
    
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showAdminNotification(title: String, message: String) {
        if (!hasNotificationPermission()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("admin_notification", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ADMIN_ID)
            .setSmallIcon(R.drawable.ic_admin)
            .setContentTitle("🔐 $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()
        
        notificationManager.notify(NOTIFICATION_ADMIN_ALERT, notification)
    }
    
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showSystemNotification(title: String, message: String) {
        if (!hasNotificationPermission()) return
        
        val notification = NotificationCompat.Builder(context, CHANNEL_SYSTEM_ID)
            .setSmallIcon(R.drawable.ic_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
    
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
    
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    fun requestNotificationPermission(activity: android.app.Activity, requestCode: Int = 1001) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                activity.requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    requestCode
                )
            }
        }
    }
}
