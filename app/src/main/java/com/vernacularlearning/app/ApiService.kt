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

    @POST("generate-worksheet")
    fun generateWorksheet(
        @Body request: WorksheetRequest
    ): Call<WorksheetResponse>

    @POST("generate-flashcards")
    fun generateFlashcards(
        @Body request: FlashcardRequest
    ): Call<FlashcardResponse>
}

data class WorksheetRequest(
    val learning_objective: String,
    val grade: String = "Class 1",
    val subject: String = "Mathematics",
    val instruction_language: String = "Hindi",
    val mother_tongue: String = "Santali"
)

data class WorksheetActivity(
    val activity_type: String,
    val instruction_hindi: String,
    val instruction_santali: String,
    val question_hindi: String,
    val question_santali: String,
    val options: List<String>,
    val correct_answer: String,
    val speaking_prompt: String,
    val santali_review_required: Boolean
)

data class WorksheetResponse(
    val title: String,
    val learning_objective: String,
    val grade: String,
    val subject: String,
    val nipun_alignment_note: String,
    val activities: List<WorksheetActivity>,
    val generation_mode: String? = null
)
data class FlashcardRequest(
    val prompt: String,
    val card_count: Int,
    val grade: String = "Class 1",
    val source_language: String = "Hindi",
    val target_language: String = "Santali"
)

data class GeneratedFlashcard(
    val hindi: String,
    val santali: String,
    val emoji: String,
    val category: String
)

data class FlashcardResponse(
    val flashcards: List<GeneratedFlashcard>,
    val status: String
)