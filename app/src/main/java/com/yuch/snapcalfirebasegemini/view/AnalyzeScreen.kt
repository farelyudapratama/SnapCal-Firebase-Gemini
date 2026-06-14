package com.yuch.snapcalfirebasegemini.view

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.data.api.response.FoodDetectionByMyModelResult
import com.yuch.snapcalfirebasegemini.data.model.EditableFoodData
import com.yuch.snapcalfirebasegemini.utils.calculatePortionValue
import com.yuch.snapcalfirebasegemini.utils.normalizeDecimal
import com.yuch.snapcalfirebasegemini.view.analyze.components.AnalyzeEditableAnalysisCard
import com.yuch.snapcalfirebasegemini.view.analyze.components.AnalyzeImagePreview
import com.yuch.snapcalfirebasegemini.view.analyze.components.AnalyzeLoadingContent
import com.yuch.snapcalfirebasegemini.view.analyze.components.AnalyzeServiceSelectionCard
import com.yuch.snapcalfirebasegemini.view.analyze.components.AnalyzeYoloDetectionDialog
import com.yuch.snapcalfirebasegemini.viewmodel.FoodEntryViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.FoodViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeScreen(
    imagePath: String,
    viewModel: FoodViewModel,
    entryViewModel: FoodEntryViewModel,
    onBack: () -> Unit,
    onSuccessfulUpload: () -> Boolean,
) {
    var selectedService by remember { mutableStateOf<String?>(null) }
    val analysisResult by viewModel.analysisResult.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSaving by entryViewModel.isLoading.collectAsStateWithLifecycle()
    val isLoading = isAnalyzing || isSaving
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val entryErrorMessage by entryViewModel.errorMessage.collectAsStateWithLifecycle()
    val uploadSuccess by entryViewModel.uploadSuccess.collectAsStateWithLifecycle()
    val yoloDetections by viewModel.yoloDetectionResult.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // State for storing analyzed food data
    var baseNutrition by remember { mutableStateOf<EditableFoodData?>(null) }
    // State for storing editable data
    var editableFood by remember { mutableStateOf<EditableFoodData?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // State for manual overrides
    val manualOverrides = remember {
        mutableStateMapOf(
            "calories" to false,
            "carbs" to false,
            "protein" to false,
            "totalFat" to false,
            "saturatedFat" to false,
            "fiber" to false,
            "sugar" to false
        )
    }


    LaunchedEffect(analysisResult) {
        analysisResult?.let {
            val baseData = EditableFoodData(
                foodName = it.foodName,
                calories = it.calories.toString().normalizeDecimal(),
                carbs = it.carbs.toString().normalizeDecimal(),
                protein = it.protein.toString().normalizeDecimal(),
                totalFat = it.totalFat.toString().normalizeDecimal(),
                saturatedFat = it.saturatedFat.toString().normalizeDecimal(),
                fiber = it.fiber.toString().normalizeDecimal(),
                sugar = it.sugar.toString().normalizeDecimal(),
                weightInGrams = "100"
            )
            baseNutrition = baseData
            editableFood = baseData.copy()
            manualOverrides.keys.forEach { key -> manualOverrides[key] = false } // Reset overrides
        }
    }

    LaunchedEffect(editableFood?.weightInGrams) {
        val base = baseNutrition
        val weight = editableFood?.weightInGrams?.toFloatOrNull()
        if (base != null && weight != null) {
            editableFood = editableFood?.copy(
                calories = if (!manualOverrides["calories"]!!) calculatePortionValue(base.calories, weight.toString()) else editableFood?.calories ?: "",
                carbs = if (!manualOverrides["carbs"]!!) calculatePortionValue(base.carbs, weight.toString()) else editableFood?.carbs ?: "",
                protein = if (!manualOverrides["protein"]!!) calculatePortionValue(base.protein, weight.toString()) else editableFood?.protein ?: "",
                totalFat = if (!manualOverrides["totalFat"]!!) calculatePortionValue(base.totalFat, weight.toString()) else editableFood?.totalFat ?: "",
                saturatedFat = if (!manualOverrides["saturatedFat"]!!) calculatePortionValue(base.saturatedFat, weight.toString()) else editableFood?.saturatedFat ?: "",
                fiber = if (!manualOverrides["fiber"]!!) calculatePortionValue(base.fiber, weight.toString()) else editableFood?.fiber ?: "",
                sugar = if (!manualOverrides["sugar"]!!) calculatePortionValue(base.sugar, weight.toString()) else editableFood?.sugar ?: "",
            )
        }
    }

    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            Toast.makeText(
                context,
                context.getString(R.string.food_entry_added_successfully),
                Toast.LENGTH_SHORT
            ).show()

            entryViewModel.resetState()
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

    LaunchedEffect(entryErrorMessage) {
        entryErrorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            entryViewModel.clearErrorMessage()
        }
    }

    // Handle success messages with Snackbar
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearErrorMessage()
        }
    }

    var loadingPhase by remember { mutableStateOf<String?>(null) } // "analyzing", "uploading", or null

    LaunchedEffect(isLoading) {
        loadingPhase = if (isLoading) {
            if (analysisResult == null) "analyzing" else "uploading"
        } else {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.food_analysis_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )
        },
        floatingActionButton = {
            if (editableFood != null && !isLoading) {
                FloatingActionButton(
                    onClick = {
                        editableFood?.let { foodData ->
                            if (foodData.mealType != null) {
                                entryViewModel.uploadFood(imagePath, foodData)
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.please_select_a_meal_type_before_saving),
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
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
                // Enhanced Image Preview with animation
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    AnalyzeImagePreview(imagePath)
                }
            }

            item {
                when {
                    isLoading -> {
                        AnalyzeLoadingContent(phase = loadingPhase)
                    }
                    editableFood == null -> {
                        AnalyzeServiceSelectionCard(
                            selectedService = selectedService,
                            onServiceSelected = { selectedService = it },
                            onAnalyzeClick = { service ->
                                viewModel.analyzeImage(imagePath, service)
                            },
                            isLoading = isLoading,
                            onMyModelClick = {
                                viewModel.analyzeFoodByMyModel(imagePath)
                            }
                        )
                    }
                    else -> {
                        // Main nutrition info card
                        AnalyzeEditableAnalysisCard(
                            foodData = editableFood!!,
                            manualOverrides = manualOverrides,
                            onValueChange = { editableFood = it }
                        )

                        // TODO: Food Composition Section
                    }
                }
            }

            // Add YOLO detection chips and actions
            yoloDetections?.let { detections ->
                if (detections.isNotEmpty()) {
                    item {
                        Text(
                            text = "Detected Foods:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        val selectedDetections = remember { mutableStateListOf<FoodDetectionByMyModelResult>() }
                        var additionalDescription by remember { mutableStateOf("") }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(detections) { detection ->
                                val isSelected = selectedDetections.contains(detection)

                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) {
                                            selectedDetections.remove(detection)
                                        } else {
                                            selectedDetections.add(detection)
                                        }
                                    },
                                    label = { Text("${detection.foodName} (${String.format(Locale.getDefault(), "%.1f%%", detection.confidence * 100)})") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Additional description input
                        OutlinedTextField(
                            value = additionalDescription,
                            onValueChange = { additionalDescription = it },
                            label = { Text("Additional Description (Optional)") },
                            placeholder = { Text("e.g., with rice, extra spicy, etc.") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (selectedDetections.isNotEmpty()) {
                                        val selectedFoodNames = selectedDetections.joinToString(", ") { it.foodName }
                                        viewModel.estimateNutritionByName(
                                            selectedFoodNames,
                                            additionalDescription.takeIf { it.isNotBlank() }
                                        )
                                        viewModel.clearYoloDetections()
                                    }
                                },
                                enabled = selectedDetections.isNotEmpty() && !isLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Continue Analysis")
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.clearYoloDetections()
                                    selectedService = "gemini"
                                    viewModel.analyzeImage(imagePath, "gemini")
                                },
                                enabled = !isLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Use AI Analysis")
                            }
                        }
                    }
                }
            }
        }

        // YOLO Detection Dialog - Show as overlay outside LazyColumn
        yoloDetections?.let { detections ->
            if (detections.isNotEmpty()) {
                AnalyzeYoloDetectionDialog(
                    detections = detections,
                    onContinueAnalysis = { selectedFood, description ->
                        viewModel.estimateNutritionByName(selectedFood, description)
                        viewModel.clearYoloDetections() // Clear after processing
                    },
                    onDirectAiAnalysis = {
                        viewModel.clearYoloDetections()
                        selectedService = "gemini"
                        viewModel.analyzeImage(imagePath, "gemini")
                    },
                    onDismiss = {
                        viewModel.clearYoloDetections()
                    },
                    isLoading = isLoading
                )
            }
        }
    }
}
