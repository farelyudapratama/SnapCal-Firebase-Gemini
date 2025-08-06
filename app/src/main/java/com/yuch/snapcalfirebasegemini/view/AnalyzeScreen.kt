package com.yuch.snapcalfirebasegemini.view

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.data.api.response.FoodDetectionByMyModelResult
import com.yuch.snapcalfirebasegemini.data.model.EditableFoodData
import com.yuch.snapcalfirebasegemini.utils.normalizeDecimal
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

    // Show food composition section toggle
    // TODO: This will control whether to show the food composition section
//    var showComposition by remember { mutableStateOf(false) }

    // Update editable data when new analysis results arrive
    LaunchedEffect(analysisResult) {
        analysisResult.data?.let {
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

            viewModel.resetState()
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
            if (analysisResult.data == null) "analyzing" else "uploading"
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
                                viewModel.uploadFood(imagePath, foodData)
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
                    ImagePreview(imagePath)
                }
            }

            item {
                when {
                    isLoading -> {
                        LoadingContent(phase = loadingPhase)
                    }
                    editableFood == null -> {
                        ServiceSelectionCard(
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
                        EditableAnalysisCard(
                            foodData = editableFood!!,
                            manualOverrides = manualOverrides,
                            onValueChange = { editableFood = it }
                        )

                        // TODO: Food Composition Section - This section will display detailed
                        // ingredients and composition information for the analyzed food
//                        CompositionSectionPlaceholder(
//                            showComposition = showComposition,
//                            onToggleComposition = { showComposition = it }
//                        )
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
                YoloDetectionDialog(
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

@Composable
private fun LoadingContent(phase: String?) {
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
                modifier = Modifier.size(56.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 5.dp
            )
            Text(
                stringResource(
                    when (phase) {
                        "analyzing" -> R.string.analyzing_your_food
                        "uploading" -> R.string.saving_your_food
                        else -> R.string.analyzing_your_food
                    }
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                stringResource(R.string.this_may_take_a_moment),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ServiceSelectionCard(
    selectedService: String?,
    onServiceSelected: (String) -> Unit,
    onAnalyzeClick: (String) -> Unit,
    isLoading: Boolean,
    onMyModelClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Pilih Metode Analisis:",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            // My Model Analysis - Terbaik (First)
            AnalysisMethodCard(
                title = "Analisis dengan Model Yolo",
                description = "Deteksi otomatis menggunakan sistem lokal, lalu dihitung nutrisinya dengan AI.",
                buttonText = "Analisis dengan Model Saya",
//                isRecommended = true,
                onClick = onMyModelClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // AI Service Selection
            Text(
                "Atau pilih layanan AI eksternal:",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp)
            )

            // AI Service Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ServiceButton(
                    text = "Gemini",
                    description = "Google AI",
                    isSelected = selectedService == "gemini",
                    onClick = { onServiceSelected("gemini") },
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.google_gemini_icon
                )

                ServiceButton(
                    text = "🦙 LLaMA",
                    description = "Meta AI",
                    isSelected = selectedService == "groq",
                    onClick = { onServiceSelected("groq") },
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                "Gambar dikirim ke AI eksternal untuk analisis visual dan estimasi nutrisi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // AI Analysis Button
            Button(
                onClick = { selectedService?.let(onAnalyzeClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedService != null && !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Analisis dengan ${if (selectedService == "gemini") "Gemini" else "LLaMA"}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalysisMethodCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    isRecommended: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = contentColor
                )

                if (isRecommended) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            "Terpopuler",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecommended)
                        MaterialTheme.colorScheme.primary
                    else
                        contentColor.copy(alpha = 0.1f),
                    contentColor = if (isRecommended)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        contentColor
                )
            ) {
                Text(
                    buttonText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EditableAnalysisCard(
    foodData: EditableFoodData,
    manualOverrides: MutableMap<String, Boolean>,
    onValueChange: (EditableFoodData) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.food_information),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                stringResource(R.string.adjust_values_if_needed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            val mealTypeMap = mapOf(
                "breakfast" to stringResource(R.string.breakfast),
                "lunch" to stringResource(R.string.lunch),
                "dinner" to stringResource(R.string.dinner),
                "snack" to stringResource(R.string.snack),
                "drink" to stringResource(R.string.drink)
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = foodData.mealType
                        ?.let { mealTypeMap[it] }
                        ?.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                            else it.toString()
                        }
                        ?: stringResource(R.string.select_meal_type),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.meal_type)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf(
                        stringResource(R.string.breakfast) to "breakfast",
                        stringResource(R.string.lunch) to "lunch",
                        stringResource(R.string.dinner) to "dinner",
                        stringResource(R.string.snack) to "snack",
                        stringResource(R.string.drink) to "drink"
                    ).forEach { (label, value) ->
                        DropdownMenuItem(
                            text = {
                                Text(label.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(Locale.ROOT)
                                    else it.toString()
                                })
                            },
                            onClick = {
                                onValueChange(foodData.copy(mealType = value))
                                expanded = false
                            }
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = foodData.foodName,
                onValueChange = { onValueChange(foodData.copy(foodName = it)) },
                label = { Text(stringResource(R.string.food_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            EditableNutritionRow(stringResource(R.string.weight_g), foodData.weightInGrams) {
                onValueChange(foodData.copy(weightInGrams = it.normalizeDecimal()))
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                stringResource(R.string.nutrition_values),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            val nutritionFields = listOf(
                "calories" to stringResource(R.string.nutrient_calories) + " (kcal)",
                "carbs" to stringResource(R.string.nutrient_carbs) + " (g)",
                "protein" to stringResource(R.string.nutrient_protein) + " (g)",
                "totalFat" to stringResource(R.string.nutrient_fat) + " (g)",
                "saturatedFat" to "Saturated Fat (g)",
                "fiber" to stringResource(R.string.nutrient_fiber) + " (g)",
                "sugar" to stringResource(R.string.nutrient_sugar) + " (g)"
            )

            nutritionFields.forEach { (key, label) ->
                val value = when (key) {
                    "calories" -> foodData.calories
                    "carbs" -> foodData.carbs
                    "protein" -> foodData.protein
                    "totalFat" -> foodData.totalFat
                    "saturatedFat" -> foodData.saturatedFat
                    "fiber" -> foodData.fiber
                    "sugar" -> foodData.sugar
                    else -> ""
                }

                ToggleableNutritionRow(
                    label = label,
                    value = value,
                    isManual = manualOverrides[key] == true,
                    onValueChange = { newValue ->
                        manualOverrides[key] = true
                        onValueChange(updateField(foodData, key, newValue.normalizeDecimal()))
                    },
                    onToggleChange = { isChecked ->
                        manualOverrides[key] = isChecked
                    }
                )
            }
        }
    }
}

@Composable
private fun ToggleableNutritionRow(
    label: String,
    value: String,
    isManual: Boolean,
    onValueChange: (String) -> Unit,
    onToggleChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Edit",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isManual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = isManual,
                    onCheckedChange = onToggleChange,
                    thumbContent = if (isManual) {
                        {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else null
                )
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = isManual,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isManual)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.outline
            )
        )
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
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
private fun ImagePreview(imagePath: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.height(280.dp)) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = "Food image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun ServiceButton(
    text: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconRes: Int? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
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
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                )

                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun YoloDetectionDialog(
    detections: List<FoodDetectionByMyModelResult>,
    onContinueAnalysis: (selectedFoods: String, description: String?) -> Unit,
    onDirectAiAnalysis: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    var selectedDetections by remember { mutableStateOf(setOf<FoodDetectionByMyModelResult>()) }
    var additionalDescription by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Makanan Terdeteksi",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Pilih makanan yang sesuai dengan gambar Anda:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Food detection chips with checkbox behavior
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(detections) { detection ->
                        val isSelected = selectedDetections.contains(detection)

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedDetections = if (isSelected) {
                                    selectedDetections - detection
                                } else {
                                    selectedDetections + detection
                                }
                            },
                            label = {
                                Text(
                                    text = "${detection.foodName} (${String.format(Locale.getDefault(), "%.1f%%", detection.confidence * 100)})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            enabled = true,
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline,
                                selectedBorderColor = Color.Transparent,
                                borderWidth = 1.dp
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Additional description input
                OutlinedTextField(
                    value = additionalDescription,
                    onValueChange = { additionalDescription = it },
                    label = { Text("Deskripsi Tambahan (Opsional)") },
                    placeholder = { Text("Contoh: dengan nasi, pedas extra, dll.") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDirectAiAnalysis,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Analisis Langsung AI")
                    }

                    Button(
                        onClick = {
                            if (selectedDetections.isNotEmpty()) {
                                val selectedFoodNames = selectedDetections.joinToString(", ") { it.foodName }
                                onContinueAnalysis(
                                    selectedFoodNames,
                                    additionalDescription.takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        enabled = selectedDetections.isNotEmpty() && !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Lanjutkan Analisis")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetectionChip(
    detection: FoodDetectionByMyModelResult,
    isSelected: Boolean,
    onSelectionChanged: () -> Unit
) {
    FilterChip(
        onClick = onSelectionChanged,
        label = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    detection.foodName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    "${(detection.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        selected = isSelected,
        leadingIcon = if (isSelected) {
            {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true, // Assuming the chip is always enabled
            selected = isSelected, // Pass the isSelected state
            borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline,
            selectedBorderColor = Color.Transparent,
            borderWidth = 1.dp
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

fun updateField(food: EditableFoodData, field: String, value: String): EditableFoodData {
    return when (field) {
        "calories" -> food.copy(calories = value)
        "carbs" -> food.copy(carbs = value)
        "protein" -> food.copy(protein = value)
        "totalFat" -> food.copy(totalFat = value)
        "saturatedFat" -> food.copy(saturatedFat = value)
        "fiber" -> food.copy(fiber = value)
        "sugar" -> food.copy(sugar = value)
        else -> food
    }
}

fun calculatePortionValue(valuePer100g: String, weight: String): String {
    val value = valuePer100g.toFloatOrNull() ?: 0f
    val grams = weight.toFloatOrNull() ?: 100f
    val result =  ((value / 100f) * grams)
    return String.format(Locale.getDefault(), "%.2f", result)
}
