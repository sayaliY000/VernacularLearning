package com.vernacularlearning.app

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

data class HealthResponse(
    val status: String
)

data class TranslationRequest(
    val text: String,
    val source_language: String,
    val target_language: String
)

data class TranslationResponse(
    val original_text: String,
    val source_language: String,
    val target_language: String,
    val translated_text: String?,
    val status: String
)

interface ApiService {

    @GET("health")
    fun checkHealth(): Call<HealthResponse>

    @POST("translate")
    fun translateText(
        @Body request: TranslationRequest
    ): Call<TranslationResponse>
}