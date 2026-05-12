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
        
        // Verificar patrones de desinformación comunes
        val suspiciousPatterns = detectSuspiciousPatterns(input)
        
        val prompt = """
            Eres un verificador de noticias profesional especializado en análisis de contenido textual.
            Analiza el siguiente texto de noticia usando tu conocimiento general y bases de datos verificadas.
            
            REGLAS ESTRICTAS:
            - Analiza SOLAMENTE el texto proporcionado, no intentes acceder a URLs externas
            - Busca patrones de desinformación, lenguaje sensacionalista y falta de fuentes
            - Con fuentes verificables mencionadas = "Fiable"
            - Con información parcial o sin fuentes = "Dudosa"  
            - Con evidencias claras de manipulación o falsedad = "Falsa"
            - Responde SIEMPRE en español
            - Devuelve SOLO el JSON sin markdown ni texto extra
            - NUNCA inventes fuentes ni datos
            
            Texto a analizar: "$input"
            
            Patrones detectados: ${suspiciousPatterns.joinToString(", ")}
            
            Devuelve exactamente este JSON:
            {
              "fiabilidad": número entre 0 y 100,
              "veredicto": "Fiable" o "Dudosa" o "Falsa",
              "explicacion": "explicación en español máximo 150 palabras",
              "fuentes": ["fuente1", "fuente2"],
              "patrones_detectados": ["patrón1", "patrón2"],
              "recomendaciones": ["recomendación1", "recomendación2"]
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
                    ?.text ?: return fallbackResult(suspiciousPatterns)
                    
                parseGeminiResponse(text, suspiciousPatterns)
            } else {
                android.util.Log.e("NewsRepo", 
                    "Error ${response.code()}: ${response.errorBody()?.string()}")
                fallbackResult(suspiciousPatterns)
            }
        } catch (e: Exception) {
            android.util.Log.e("NewsRepo", "Exception: ${e.message}")
            fallbackResult(suspiciousPatterns)
        }
    }

    private fun detectSuspiciousPatterns(input: String): List<String> {
        val patterns = mutableListOf<String>()
        val text = input.lowercase()
        
        // Patrones de lenguaje sensacionalista
        if (text.contains("shock") || text.contains("impactante") || text.contains("nunca antes visto")) {
            patterns.add("Lenguaje sensacionalista")
        }
        
        // Falta de fuentes específicas
        if (!text.contains("según") && !text.contains("fuentes") && !text.contains("informó") && 
            !text.contains("declaró") && !text.contains("dijo")) {
            patterns.add("Ausencia de fuentes")
        }
        
        // Títulos en mayúsculas excesivas
        if (input.split("\n").firstOrNull()?.count { it.isUpperCase() }?.let { it > 10 } == true) {
            patterns.add("Uso excesivo de mayúsculas")
        }
        
        // Preguntas retóricas
        if (text.contains("?") && text.count { it == '?' } > 2) {
            patterns.add("Uso excesivo de preguntas")
        }
        
        // Números sin contexto
        if (Regex("\\d+%").find(text) != null && !text.contains("según") && !text.contains("estudio")) {
            patterns.add("Porcentajes sin fuente")
        }
        
        return patterns
    }

    private fun parseGeminiResponse(text: String, suspiciousPatterns: List<String>): AnalysisResult {
        return try {
            // Limpiar el texto para obtener JSON puro
            val cleanText = text.replace("```json", "").replace("```", "").trim()
            
            // Parsear JSON manualmente para evitar dependencias
            val json = org.json.JSONObject(cleanText)
            
            val fiabilidad = json.getDouble("fiabilidad") / 100.0
            val veredictoStr = json.getString("veredicto")
            val explicacion = json.getString("explicacion")
            
            val fuentes = if (json.has("fuentes")) {
                val fuentesArray = json.getJSONArray("fuentes")
                (0 until fuentesArray.length()).map { fuentesArray.getString(it) }
            } else emptyList()
            
            val patronesDetectados = if (json.has("patrones_detectados")) {
                val patronesArray = json.getJSONArray("patrones_detectados")
                (0 until patronesArray.length()).map { patronesArray.getString(it) }
            } else emptyList()
            
            val recomendaciones = if (json.has("recomendaciones")) {
                val recomendacionesArray = json.getJSONArray("recomendaciones")
                (0 until recomendacionesArray.length()).map { recomendacionesArray.getString(it) }
            } else emptyList()
            
            val verdict = when (veredictoStr) {
                "Fiable" -> Verdict.FIABLE
                "Dudosa" -> Verdict.DUDOSA
                "Falsa" -> Verdict.FALSA
                else -> Verdict.DUDOSA
            }
            
            AnalysisResult(
                verdict = verdict,
                confidenceScore = fiabilidad,
                explanation = explicacion,
                detectedPatterns = suspiciousPatterns + patronesDetectados,
                sourcesChecked = fuentes.size,
                similarReliableArticles = fuentes,
                blacklistedDomainMatch = false,
                alternativeNewsTitle = "",
                alternativeNewsUrl = "",
                alternativeNewsDescription = ""
            )
        } catch (e: Exception) {
            android.util.Log.e("NewsRepo", "JSON parsing error: ${e.message}")
            fallbackResult(suspiciousPatterns)
        }
    }

    private fun fallbackResult(detectedPatterns: List<String> = emptyList()): AnalysisResult {
        return AnalysisResult(
            verdict = Verdict.DUDOSA,
            confidenceScore = 0.5,
            explanation = "No se pudo verificar la noticia debido a un error en el análisis. Por favor, intenta nuevamente.",
            detectedPatterns = detectedPatterns,
            sourcesChecked = 0,
            similarReliableArticles = emptyList(),
            blacklistedDomainMatch = false
        )
    }

    // Mantener la función de lista negra para referencia histórica
    private suspend fun checkBlacklist(input: String): Boolean {
        // Extraer dominio si es URL, pero ya que analizamos texto, esto es menos relevante
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val blacklistCollection = firestore.collection("blacklist")
            
            // Buscar palabras clave sospechosas en lugar de dominios
            val suspiciousWords = listOf("falso", "mentira", "engaño", "bulos")
            val text = input.lowercase()
            
            for (word in suspiciousWords) {
                if (text.contains(word)) {
                    // Verificar si esta palabra está en la lista negra
                    val snapshot = blacklistCollection.whereEqualTo("keyword", word).get().await()
                    if (!snapshot.isEmpty) return true
                }
            }
            false
        } catch (e: Exception) {
            android.util.Log.e("NewsRepo", "Blacklist check error: ${e.message}")
            false
        }
    }
}
