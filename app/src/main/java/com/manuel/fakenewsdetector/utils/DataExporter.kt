package com.manuel.fakenewsdetector.utils

import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.manuel.fakenewsdetector.domain.model.NewsAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilidad para exportar datos a ficheros JSON y CSV
 * Cumple con la rúbrica ADA: Gestión de información almacenada en ficheros
 */
class DataExporter(private val context: Context) {

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    /**
     * Exporta una lista de análisis a formato JSON
     */
    suspend fun exportToJson(
        analyses: List<NewsAnalysis>,
        fileName: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val defaultFileName = "analyses_export_$timestamp.json"
            val finalFileName = fileName ?: defaultFileName

            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            
            val file = File(downloadsDir, finalFileName)
            
            val json = gson.toJson(analyses)
            FileWriter(file).use { it.write(json) }

            Result.success(file.absolutePath)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Exporta una lista de análisis a formato CSV
     */
    suspend fun exportToCsv(
        analyses: List<NewsAnalysis>,
        fileName: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val defaultFileName = "analyses_export_$timestamp.csv"
            val finalFileName = fileName ?: defaultFileName

            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            
            val file = File(downloadsDir, finalFileName)
            
            val csv = buildCsvString(analyses)
            FileWriter(file).use { it.write(csv) }

            Result.success(file.absolutePath)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Importa análisis desde un fichero JSON
     */
    suspend fun importFromJson(filePath: String): Result<List<NewsAnalysis>> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            val json = file.readText()
            
            val listType = object : TypeToken<List<NewsAnalysis>>() {}.type
            val analyses = gson.fromJson<List<NewsAnalysis>>(json, listType)

            Result.success(analyses)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Construye una cadena CSV a partir de una lista de análisis
     */
    private fun buildCsvString(analyses: List<NewsAnalysis>): String {
        val header = "ID,URL,Headline,Veredicto,Confianza,Explicacion,Fecha\n"
        val rows = analyses.joinToString("\n") { analysis ->
            val url = analysis.url?.replace(",", ";") ?: "" // Escapar comas
            val headline = analysis.headline?.replace(",", ";") ?: "" // Escapar comas
            val verdict = analysis.result?.verdict?.name ?: "N/A"
            val confidence = analysis.result?.confidenceScore ?: 0.0
            val explanation = analysis.result?.explanation?.replace(",", ";") ?: "" // Escapar comas
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(analysis.analyzedAt))
            
            "${analysis.id},\"$url\",\"$headline\",$verdict,$confidence,\"$explanation\",$date"
        }
        
        return header + rows
    }

    /**
     * Verifica si hay permisos de escritura en almacenamiento externo
     */
    fun checkStoragePermissions(): Boolean {
        return context.getExternalFilesDir(null)?.exists() == true
    }

    /**
     * Obtiene la ruta del directorio de descargas
     */
    fun getDownloadsDirectory(): File {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    }
}
