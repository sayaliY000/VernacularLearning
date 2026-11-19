
package com.vernacularlearning.app

import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class Flashcard(
    val hindi: String,
    val santali: String,
    val emoji: String,
    val category: String
)

@Composable
fun FlashcardsScreen(onBack: () -> Unit) {

    val context = LocalContext.current

    var speechEngine by remember {
        mutableStateOf<TextToSpeech?>(null)
    }

    var isSpeechReady by remember {
        mutableStateOf(false)
    }

    DisposableEffect(context) {
        val engine = TextToSpeech(context) { status ->
            isSpeechReady = status == TextToSpeech.SUCCESS
        }

        speechEngine = engine

        onDispose {
            engine.stop()
            engine.shutdown()
        }
    }

    var flashcards by remember {
        mutableStateOf(
            listOf(
                Flashcard("नमस्ते", "जोहार", "🙏", "Greetings"),
                Flashcard("पानी", "दाक्", "💧", "Daily Words"),
                Flashcard("सूरज", "सिंगि", "☀️", "Nature"),
                Flashcard("घर", "ओड़ाक्", "🏠", "Daily Words"),
                Flashcard("माँ", "आयो", "👩", "Family"),
                Flashcard("पेड़", "दारू", "🌳", "Nature"),
                Flashcard("गाय", "गाइ", "🐄", "Animals"),
                Flashcard("फल", "जोन", "🍎", "Food")
            )
        )
    }

    var customPrompt by remember { mutableStateOf("") }
    var cardCount by remember { mutableIntStateOf(10) }
    var isGenerating by remember { mutableStateOf(false) }
    var generationMessage by remember { mutableStateOf("") }
    var currentIndex by remember { mutableIntStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }

    val currentCard = flashcards[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FC))
            .padding(20.dp)
    ) {

        TextButton(onClick = onBack) {
            Text("← Back")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "🃏 Flashcards",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF173A70)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Learn Hindi and Santali words",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "✨ Create Your Own Flashcards",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF173A70)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = customPrompt,
            onValueChange = { customPrompt = it },
            label = { Text("What should children learn?") },
            placeholder = {
                Text("Example: Animals for Class 1 in Hindi and Santali")
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Number of cards: $cardCount")

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                isGenerating = true
                generationMessage = "Generating flashcards..."

                val request = FlashcardRequest(
                    prompt = customPrompt,
                    card_count = cardCount
                )

                RetrofitClient.apiService.generateFlashcards(request)
                    .enqueue(object : Callback<FlashcardResponse> {

                        override fun onResponse(
                            call: Call<FlashcardResponse>,
                            response: Response<FlashcardResponse>
                        ) {
                            isGenerating = false

                            if (response.isSuccessful && response.body() != null) {
                                val generatedCards = response.body()!!.flashcards

                                if (generatedCards.isNotEmpty()) {
                                    flashcards = generatedCards.map {
                                        Flashcard(
                                            hindi = it.hindi,
                                            santali = it.santali,
                                            emoji = it.emoji,
                                            category = it.category
                                        )
                                    }

                                    currentIndex = 0
                                    showAnswer = false
                                    generationMessage = "Successfully generated ${flashcards.size} flashcards!"
                                } else {
                                    generationMessage = "No flashcards were returned."
                                }
                            } else {
                                generationMessage = "Generation failed. Please try again."
                            }
                        }

                        override fun onFailure(
                            call: Call<FlashcardResponse>,
                            t: Throwable
                        ) {
                            isGenerating = false
                            generationMessage = "Connection error: ${t.message}"
                        }
                    })
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = customPrompt.isNotBlank() && !isGenerating
        ) {
            Text(
                if (isGenerating) "Generating..." else "✨ Generate Flashcards"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (generationMessage.isNotBlank()) {
            Text(
                text = generationMessage,
                color = if (generationMessage.startsWith("Successfully")) {
                    Color(0xFF16834A)
                } else {
                    Color(0xFF173A70)
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(5, 10, 15, 20).forEach { count ->
                FilterChip(
                    selected = cardCount == count,
                    onClick = { cardCount = count },
                    label = { Text("$count") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable {
                    showAnswer = !showAnswer
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(5.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = currentCard.emoji,
                    fontSize = 70.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (showAnswer) {

                    Text(
                        text = "Hindi",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )

                    Text(
                        text = currentCard.hindi,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Santali",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )

                    Text(
                        text = currentCard.santali,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16834A)
                    )

                } else {

                    Text(
                        text = currentCard.hindi,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Tap the card to reveal Santali",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hindi and Santali pronunciation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = {
                    speechEngine?.let { engine ->
                        val result = engine.setLanguage(
                            Locale.forLanguageTag("hi-IN")
                        )

                        if (result != TextToSpeech.LANG_MISSING_DATA &&
                            result != TextToSpeech.LANG_NOT_SUPPORTED
                        ) {
                            engine.speak(
                                currentCard.hindi,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "HINDI_AUDIO"
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = isSpeechReady
            ) {
                Text("🔊 Hindi")
            }

            Button(
                onClick = {
                    speechEngine?.let { engine ->
                        val result = engine.setLanguage(
                            Locale.forLanguageTag("sat")
                        )

                        if (result != TextToSpeech.LANG_MISSING_DATA &&
                            result != TextToSpeech.LANG_NOT_SUPPORTED
                        ) {
                            engine.speak(
                                currentCard.santali,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "SANTALI_AUDIO"
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = isSpeechReady
            ) {
                Text("🔊 Santali")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Card ${currentIndex + 1} of ${flashcards.size}",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedButton(
                onClick = {
                    currentIndex =
                        (currentIndex - 1 + flashcards.size) % flashcards.size
                    showAnswer = false
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Previous")
            }

            Button(
                onClick = {
                    currentIndex =
                        (currentIndex + 1) % flashcards.size
                    showAnswer = false
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Next →")
            }
        }
    }
}
