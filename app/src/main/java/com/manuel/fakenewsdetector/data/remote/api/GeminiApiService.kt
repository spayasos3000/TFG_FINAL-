package com.manuel.fakenewsdetector.data.remote.api

import com.manuel.fakenewsdetector.data.remote.model.GeminiRequest
import com.manuel.fakenewsdetector.data.remote.model.GeminiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Path

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}
