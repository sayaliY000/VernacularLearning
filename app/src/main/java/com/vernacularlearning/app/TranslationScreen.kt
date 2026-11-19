
package com.vernacularlearning.app
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.speech.tts.TextToSpeech
import java.util.Locale

@Composable
fun TranslationScreen(
    onBack: () -> Unit
) {
    var hindiText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }

    val context = LocalContext.current

    var tts by remember {
        mutableStateOf<TextToSpeech?>(null)
    }

    var isTtsReady by remember {
        mutableStateOf(false)
    }

    DisposableEffect(context) {
        val speechEngine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val languageResult = tts?.setLanguage(
                    Locale.forLanguageTag("sat")
                )

                isTtsReady =
                    languageResult != TextToSpeech.LANG_MISSING_DATA &&
                            languageResult != TextToSpeech.LANG_NOT_SUPPORTED &&
                            languageResult != null
            }
        }

        tts = speechEngine

        onDispose {
            speechEngine.stop()
            speechEngine.shutdown()
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()

            if (!spokenText.isNullOrBlank()) {
                hindiText = spokenText
                errorMessage = ""
                resultMessage = ""
            }
        }

        isListening = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Translate",
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Translate a classroom sentence from Hindi to Santhali.")

        Spacer(modifier = Modifier.height(24.dp))

        Text("Source language: Hindi")

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = hindiText,
            onValueChange = {
                hindiText = it
                errorMessage = ""
                resultMessage = ""
            },
            label = { Text("Enter Hindi text") },
            placeholder = { Text("उदाहरण: नमस्ते।") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                        putExtra(
                            RecognizerIntent.EXTRA_PROMPT,
                            "अब हिंदी में बोलिए"
                        )
                    }

                    speechLauncher.launch(intent)
                } else {
                    errorMessage = "Speech recognition is not available on this phone."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🎤 Speak in Hindi")
        }

        Text("Target language: Santhali")

        Spacer(modifier = Modifier.height(8.dp))

        if (resultMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = resultMessage,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Text("Your translation will appear here.")
        }
        if (resultMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    tts?.speak(
                        resultMessage,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "SANTALI_TRANSLATION"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isTtsReady
            ) {
                Text("🔊 Listen in Santali")
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (hindiText.isBlank()) {
                    errorMessage = "Please enter a Hindi sentence first."
                } else {
                    errorMessage = ""
                    resultMessage = ""
                    isLoading = true

                    val request = TranslationRequest(
                        text = hindiText,
                        source_language = "hi",
                        target_language = "sat"
                    )

                    RetrofitClient.apiService
                        .translateText(request)
                        .enqueue(object : Callback<TranslationResponse> {

                            override fun onResponse(
                                call: Call<TranslationResponse>,
                                response: Response<TranslationResponse>
                            ) {
                                isLoading = false

                                if (response.isSuccessful) {
                                    val translation =
                                        response.body()?.translated_text

                                    if (!translation.isNullOrBlank()) {
                                        resultMessage = translation
                                    } else {
                                        errorMessage =
                                            "Translation was empty. Please try again."
                                    }
                                } else {
                                    errorMessage =
                                        "Translation failed. Error code: ${response.code()}"
                                }
                            }

                            override fun onFailure(
                                call: Call<TranslationResponse>,
                                t: Throwable
                            ) {
                                isLoading = false
                                errorMessage =
                                    "Could not connect to the server. Check your internet connection and try again."
                            }
                        })
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Translating...")
            } else {
                Text("Translate")
            }
        }
    }
}
