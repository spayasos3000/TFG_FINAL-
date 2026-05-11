package com.manuel.fakenewsdetector.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.manuel.fakenewsdetector.BuildConfig
import com.manuel.fakenewsdetector.data.remote.RetrofitClient
import com.manuel.fakenewsdetector.data.remote.model.GeminiAnalysisResult
import com.manuel.fakenewsdetector.data.remote.model.GeminiContent
import com.manuel.fakenewsdetector.data.remote.model.GeminiPart
import com.manuel.fakenewsdetector.data.remote.model.GeminiRequest
import com.manuel.fakenewsdetector.domain.model.AnalysisResult
import com.manuel.fakenewsdetector.domain.model.Verdict
import kotlinx.coroutines.tasks.await

class NewsRepository {

    suspend fun analyzeNews(input: String): AnalysisResult {
        
        val isBlacklisted = checkBlacklist(input)
        
        val prompt = """
            Eres un verificador de noticias profesional.
            Analiza la siguiente noticia usando tu conocimiento.
            
            REGLAS ESTRICTAS:
            - Sin fuentes que confirmen = veredicto "Dudosa"
            - Con fuentes fiables que confirman = "Fiable"  
            - Con evidencias de falsedad = "Falsa"
            - Responde SIEMPRE en español
            - Devuelve SOLO el JSON sin markdown ni texto extra
            - NUNCA inventes fuentes ni datos
            
            Noticia: "$input"
            Dominio en lista negra: $isBlacklisted
            
            Devuelve exactamente este JSON:
            {
              "fiabilidad": número entre 0 y 100,
              "veredicto": "Fiable" o "Dudosa" o "Falsa",
              "explicacion": "explicación en español máximo 150 palabras",
              "fuentes": ["fuente1", "fuente2"],
              "noticiaFiable": "información verificada sobre el tema"
            }
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            )
        )

        return try {
            val response = RetrofitClient.geminiApi.generateContent(
                model = "gemini-2.5-flash",
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = request
            )
            
            if (response.isSuccessful) {
                val text = response.body()
                    ?.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text ?: return fallbackResult()
                    
                parseGeminiResponse(text)
            } else {
                android.util.Log.e("NewsRepo", 
                    "Error ${response.code()}: ${response.errorBody()?.string()}")
                fallbackResult()
            }
        } catch (e: Exception) {
            android.util.Log.e("NewsRepo", "Exception: ${e.message}")
            fallbackResult()
        }
    }

    private suspend fun checkBlacklist(input: String): Boolean {
        val domain = if (input.startsWith("http")) {
            try { java.net.URL(input).host } 
            catch (e: Exception) { "" }
        } else ""
        if (domain.isEmpty()) return false
        return try {
            val snap = FirebaseFirestore.getInstance()
                .collection("blacklist")
                .whereEqualTo("domain", domain)
                .get()
                .await()
            !snap.isEmpty
        } catch (e: Exception) { false }
    }

    private fun parseGeminiResponse(text: String): AnalysisResult {
        return try {
            val clean = text.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val parsed = com.google.gson.Gson()
                .fromJson(clean, GeminiAnalysisResult::class.java)
            AnalysisResult(
                verdict = when (parsed.veredicto
                    ?.lowercase()?.trim()) {
                    "fiable" -> Verdict.FIABLE
                    "falsa" -> Verdict.FALSA
                    else -> Verdict.DUDOSA
                },
                confidenceScore = (parsed.fiabilidad ?: 50) / 100.0,
                explanation = parsed.explicacion 
                    ?: "Sin análisis disponible",
                detectedPatterns = emptyList(),
                sourcesChecked = parsed.fuentes?.size ?: 0,
                similarReliableArticles = parsed.fuentes ?: emptyList(),
                blacklistedDomainMatch = false,
                alternativeNewsTitle = parsed.noticiaFiable ?: "",
                alternativeNewsUrl = parsed.fuentes?.firstOrNull() ?: "",
                alternativeNewsDescription = ""
            )
        } catch (e: Exception) {
            fallbackResult()
        }
    }

    private fun fallbackResult() = AnalysisResult(
        verdict = Verdict.DUDOSA,
        confidenceScore = 0.5,
        explanation = "No se pudo completar el análisis. Inténtalo de nuevo.",
        detectedPatterns = emptyList(),
        sourcesChecked = 0,
        similarReliableArticles = emptyList(),
        blacklistedDomainMatch = false,
        alternativeNewsTitle = "",
        alternativeNewsUrl = "",
        alternativeNewsDescription = ""
    )
}
