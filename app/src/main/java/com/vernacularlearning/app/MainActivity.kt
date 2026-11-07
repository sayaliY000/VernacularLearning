
package com.vernacularlearning.app

import android.os.Bundle
import androidx.compose.runtime.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HomeScreen()
            }
        }
    }
}

data class LearningFeature(
    val title: String,
    val description: String,
    val symbol: String
)

@Composable
fun HomeScreen() {
    val features = listOf(
        LearningFeature("Translate", "Hindi to Santhali", "🌐"),
        LearningFeature("Voice Learning", "Speak and listen", "🎤"),
        LearningFeature("Worksheets", "Learning activities", "📚"),
        LearningFeature("Flashcards", "Learn new words", "🃏")
    )
    var showTranslation by remember { mutableStateOf(false) }
    var backendStatus by remember {
        mutableStateOf("Backend not tested yet")
    }

    if (showTranslation) {
        TranslationScreen(
            onBack = { showTranslation = false }
        )
        return
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F7FC)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "📖 VernacularLearning",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF173A70)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Learning in your language",
                fontSize = 16.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Namaste! Welcome",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("What would you like to learn today?")

            Spacer(modifier = Modifier.height(20.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(features) { feature ->
                    Card(
                        onClick = {
                            if (feature.title == "Translate") {
                                showTranslation = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(155.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 3.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(feature.symbol, fontSize = 30.sp)

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                feature.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF173A70)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                feature.description,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            Button(
                onClick = {
                    backendStatus = "Connecting..."

                    RetrofitClient.apiService.checkHealth()
                        .enqueue(object : Callback<HealthResponse> {

                            override fun onResponse(
                                call: Call<HealthResponse>,
                                response: Response<HealthResponse>
                            ) {
                                backendStatus = if (
                                    response.isSuccessful &&
                                    response.body()?.status == "ok"
                                ) {
                                    "Backend connected successfully!"
                                } else {
                                    "Server responded, but the health check failed."
                                }
                            }

                            override fun onFailure(
                                call: Call<HealthResponse>,
                                t: Throwable
                            ) {
                                backendStatus =
                                    "Connection failed: ${t.message}"
                            }
                        })
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Backend Connection")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = backendStatus,
                color = Color(0xFF173A70),
                fontSize = 14.sp
            )

        }
    }
}
