package com.manuel.fakenewsdetector.data.remote.model

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

data class GeminiCandidate(
    val content: GeminiContent? = null
)

data class GeminiAnalysisResult(
    val fiabilidad: Int? = null,
    val veredicto: String? = null,
    val explicacion: String? = null,
    val fuentes: List<String>? = null,
    val noticiaFiable: String? = null
)
