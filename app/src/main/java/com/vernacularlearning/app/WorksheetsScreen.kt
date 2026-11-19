
package com.vernacularlearning.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun WorksheetsScreen(onBack: () -> Unit) {

    var objective by remember { mutableStateOf("") }
    var worksheet by remember { mutableStateOf<WorksheetResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val selectedAnswers = remember(worksheet) {
        mutableStateMapOf<Int, String>()
    }

    val currentWorksheet = worksheet

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FC))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        TextButton(onClick = onBack) {
            Text("← Back")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "📝 Worksheet Generator",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF173A70)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Enter a learning objective to generate a worksheet.")

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = objective,
            onValueChange = { objective = it },
            label = { Text("Learning Objective") },
            placeholder = {
                Text("Example: Recognize numbers 1 to 10")
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                errorMessage = ""
                worksheet = null

                val request = WorksheetRequest(
                    learning_objective = objective.trim()
                )

                RetrofitClient.apiService
                    .generateWorksheet(request)
                    .enqueue(object : Callback<WorksheetResponse> {

                        override fun onResponse(
                            call: Call<WorksheetResponse>,
                            response: Response<WorksheetResponse>
                        ) {
                            isLoading = false

                            if (response.isSuccessful && response.body() != null) {
                                worksheet = response.body()
                            } else {
                                errorMessage =
                                    "Worksheet generation failed. Please try again."
                            }
                        }

                        override fun onFailure(
                            call: Call<WorksheetResponse>,
                            t: Throwable
                        ) {
                            isLoading = false
                            errorMessage =
                                "Cannot connect to backend: ${t.message}"
                        }
                    })
            },
            enabled = objective.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isLoading) "Generating..." else "Generate Worksheet"
            )
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = Color.Red
            )
        }

        if (currentWorksheet != null) {

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = currentWorksheet.title,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF173A70)
            )

            Text("Objective: ${currentWorksheet.learning_objective}")
            Text("Class: ${currentWorksheet.grade}")
            Text("Subject: ${currentWorksheet.subject}")

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Generation mode: ${currentWorksheet.generation_mode ?: "AI"}",
                color = Color(0xFF16834A),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "NIPUN Bharat Alignment",
                fontWeight = FontWeight.Bold
            )

            Text(currentWorksheet.nipun_alignment_note)

            Spacer(modifier = Modifier.height(20.dp))

            currentWorksheet.activities.forEachIndexed { index, activity ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Text(
                            text = "Activity ${index + 1}: ${activity.activity_type}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = Color(0xFF173A70)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(activity.instruction_hindi)

                        if (activity.instruction_santali.isNotBlank()) {
                            Text(activity.instruction_santali)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = activity.question_hindi,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (activity.question_santali.isNotBlank()) {
                            Text(activity.question_santali)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (activity.options.isNotEmpty()) {

                            activity.options.forEach { option ->

                                OutlinedButton(
                                    onClick = {
                                        selectedAnswers[index] = option
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor =
                                            if (selectedAnswers[index] == option)
                                                Color(0xFFDCE9FF)
                                            else Color.White
                                    )
                                ) {
                                    Text(option)
                                }
                            }

                            val selected = selectedAnswers[index]

                            if (selected != null) {
                                Text(
                                    text = if (selected == activity.correct_answer)
                                        "✓ सही उत्तर!"
                                    else
                                        "फिर कोशिश करें।",
                                    color = if (selected == activity.correct_answer)
                                        Color(0xFF16834A)
                                    else Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (activity.speaking_prompt.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "बोलकर अभ्यास करें:",
                                fontWeight = FontWeight.Bold
                            )

                            Text(activity.speaking_prompt)
                        }
                    }
                }
            }

            val answeredCount = selectedAnswers.size
            val score = currentWorksheet.activities
                .mapIndexedNotNull { index, activity ->
                    if (selectedAnswers[index] != null &&
                        activity.options.isNotEmpty()
                    ) {
                        if (selectedAnswers[index] == activity.correct_answer) 1 else 0
                    } else null
                }
                .sum()

            Text(
                text = "Your Score: $score / $answeredCount",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF16834A)
            )
        }
    }
}
