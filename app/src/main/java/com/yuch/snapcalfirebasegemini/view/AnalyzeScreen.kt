package com.yuch.snapcalfirebasegemini.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.yuch.snapcalfirebasegemini.data.api.response.FoodAnalysisResponse
import com.yuch.snapcalfirebasegemini.viewmodel.AnalyzeViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeScreen(
    imagePath: String,
    viewModel: AnalyzeViewModel = viewModel(),
    onBack: () -> Unit
) {
    var selectedService by remember { mutableStateOf<String?>(null) }
    val analysisResult by viewModel.analysisResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle error messages with Snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            // Clear the error message after showing
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Image Preview
            ImagePreview(imagePath)

            Spacer(modifier = Modifier.height(16.dp))

            if (analysisResult == null) {
                ServiceSelectionContent(
                    selectedService = selectedService,
                    onServiceSelected = { selectedService = it },
                    onAnalyzeClick = { service ->
                        viewModel.analyzeImage(imagePath, service)
                    },
                    isLoading = isLoading
                )
            } else {
                // Analysis Results
                LazyColumn {
                    item {
                        AnalysisResultCard(analysisResult!!)
                    }
                }
            }
        }
    }
}

@Composable
private fun ImagePreview(imagePath: String) {
    AsyncImage(
        model = File(imagePath),
        contentDescription = "Captured food image",
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun ServiceSelectionContent(
    selectedService: String?,
    onServiceSelected: (String) -> Unit,
    onAnalyzeClick: (String) -> Unit,
    isLoading: Boolean
) {
    Column {
        Text(
            "Select AI Service",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ServiceButton(
                text = "Gemini",
                isSelected = selectedService == "gemini",
                onClick = { onServiceSelected("gemini") }
            )

            ServiceButton(
                text = "Groq",
                isSelected = selectedService == "groq",
                onClick = { onServiceSelected("groq") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                selectedService?.let(onAnalyzeClick)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedService != null && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Analyze")
            }
        }
    }
}

@Composable
private fun ServiceButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = text,
            color = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AnalysisResultCard(result: FoodAnalysisResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = result.foodName,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            NutritionRow("Calories", "${result.calories} kcal")
            NutritionRow("Carbs", "${result.carbs}g")
            NutritionRow("Protein", "${result.protein}g")
            NutritionRow("Total Fat", "${result.totalFat}g")
            NutritionRow("Saturated Fat", "${result.saturatedFat}g")
            NutritionRow("Fiber", "${result.fiber}g")
            NutritionRow("Sugar", "${result.sugar}g")
        }
    }
}

@Composable
private fun NutritionRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        Text(text = value, fontWeight = FontWeight.Bold)
    }
}