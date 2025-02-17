package com.yuch.snapcalfirebasegemini.view

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.yuch.snapcalfirebasegemini.data.model.EditableFoodData
import com.yuch.snapcalfirebasegemini.viewmodel.FoodViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeScreen(
    imagePath: String,
    viewModel: FoodViewModel,
    onBack: () -> Unit,
    onSuccessfulUpload: () -> Boolean,
) {


    var selectedService by remember { mutableStateOf<String?>(null) }
    val analysisResult by viewModel.analysisResult.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val uploadSuccess by viewModel.uploadSuccess.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // State untuk menyimpan data yang bisa diedit
    var editableFood by remember { mutableStateOf<EditableFoodData?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    // Update editable data ketika mendapat hasil analisis baru
    LaunchedEffect(analysisResult) {
        analysisResult.let { result ->
            editableFood =
                result.data?.let {
                    EditableFoodData(
                        foodName = it.foodName,
                        calories = it.calories.toString(),
                        carbs = it.carbs.toString(),
                        protein = it.protein.toString(),
                        totalFat = it.totalFat.toString(),
                        saturatedFat = it.saturatedFat.toString(),
                        fiber = it.fiber.toString(),
                        sugar = it.sugar.toString()
                    )
                }
        }
    }

    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            Toast.makeText(
                context,
                "Food entry added successfully!",
                Toast.LENGTH_SHORT
            ).show()

            viewModel.resetState()

            // Pop sampai ke List screen
            onSuccessfulUpload()
        }
    }

    // Handle error messages with Snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Food Analysis",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (editableFood != null && !isLoading) {
                FloatingActionButton(
                    onClick = {
                        editableFood?.let { foodData ->
                            if (foodData.mealType != null) {
                                viewModel.uploadFood(imagePath, foodData)
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Please select a meal type before saving",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save")
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ImagePreview(imagePath)
            }

            item {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 4.dp
                                )
                                Text(
                                    "Loading...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    editableFood == null -> {
                        ServiceSelectionContent(
                            selectedService = selectedService,
                            onServiceSelected = { selectedService = it },
                            onAnalyzeClick = { service ->
                                viewModel.analyzeImage(imagePath, service)
                            },
                            isLoading = isLoading
                        )
                    }
                    else -> {
                        EditableAnalysisCard(
                            foodData = editableFood!!,
                            onValueChange = { editableFood = it }
                        )
                    }
                }
            }

            // Tambah space di bawah untuk menghindari FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
private fun EditableAnalysisCard(
    foodData: EditableFoodData,
    onValueChange: (EditableFoodData) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                "Nutrition Information",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = foodData.mealType?.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                        else it.toString()
                    } ?: "Select Meal Type",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Meal Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("breakfast", "lunch", "dinner", "snack", "drink").forEach { mealType ->
                        DropdownMenuItem(
                            text = {
                                Text(mealType.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(Locale.ROOT)
                                    else it.toString()
                                })
                            },
                            onClick = {
                                onValueChange(foodData.copy(mealType = mealType))
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = foodData.foodName,
                onValueChange = { onValueChange(foodData.copy(foodName = it)) },
                label = { Text("Food Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            EditableNutritionRow("Calories (kcal)", foodData.calories) {
                onValueChange(foodData.copy(calories = it))
            }

            EditableNutritionRow("Carbs (g)", foodData.carbs) {
                onValueChange(foodData.copy(carbs = it))
            }

            EditableNutritionRow("Protein (g)", foodData.protein) {
                onValueChange(foodData.copy(protein = it))
            }

            EditableNutritionRow("Total Fat (g)", foodData.totalFat) {
                onValueChange(foodData.copy(totalFat = it))
            }

            EditableNutritionRow("Saturated Fat (g)", foodData.saturatedFat) {
                onValueChange(foodData.copy(saturatedFat = it))
            }

            EditableNutritionRow("Fiber (g)", foodData.fiber) {
                onValueChange(foodData.copy(fiber = it))
            }

            EditableNutritionRow("Sugar (g)", foodData.sugar) {
                onValueChange(foodData.copy(sugar = it))
            }
        }
    }
}

@Composable
private fun EditableNutritionRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
private fun ImagePreview(imagePath: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        AsyncImage(
            model = File(imagePath),
            contentDescription = "Captured food image",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ServiceSelectionContent(
    selectedService: String?,
    onServiceSelected: (String) -> Unit,
    onAnalyzeClick: (String) -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                "Select AI Service",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ServiceButton(
                    text = "Gemini",
                    isSelected = selectedService == "gemini",
                    onClick = { onServiceSelected("gemini") },
                    modifier = Modifier.weight(1f)
                )

                ServiceButton(
                    text = "Groq",
                    isSelected = selectedService == "groq",
                    onClick = { onServiceSelected("groq") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { selectedService?.let(onAnalyzeClick) },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedService != null && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Analyze",
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        )
    }
}