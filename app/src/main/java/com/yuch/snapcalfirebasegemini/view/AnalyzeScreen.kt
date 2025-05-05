package com.yuch.snapcalfirebasegemini.view

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.yuch.snapcalfirebasegemini.R
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
    var showComposition by remember { mutableStateOf(false) }

    // Update editable data when new analysis results arrive
    LaunchedEffect(analysisResult) {
        analysisResult.data?.let {
            val baseData = EditableFoodData(
                foodName = it.foodName,
                calories = it.calories.toString(),
                carbs = it.carbs.toString(),
                protein = it.protein.toString(),
                totalFat = it.totalFat.toString(),
                saturatedFat = it.saturatedFat.toString(),
                fiber = it.fiber.toString(),
                sugar = it.sugar.toString(),
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
                        LoadingContent()
                    }
                    editableFood == null -> {
                        ServiceSelectionCard(
                            selectedService = selectedService,
                            onServiceSelected = { selectedService = it },
                            onAnalyzeClick = { service ->
                                viewModel.analyzeImage(imagePath, service)
                            },
                            onTFLiteClick = { viewModel.analyzeWithTFLite(imagePath, context) },
                            isLoading = isLoading
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
                        CompositionSectionPlaceholder(
                            showComposition = showComposition,
                            onToggleComposition = { showComposition = it }
                        )
                    }
                }
            }

            // Add space at the bottom to avoid FAB overlap
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun LoadingContent() {
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
                stringResource(R.string.analyzing_your_food),
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
    onTFLiteClick: () -> Unit,
    isLoading: Boolean
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
                .fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.choose_analysis_method),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ServiceButton(
                    text = "Gemini",
                    isSelected = selectedService == "gemini",
                    onClick = { onServiceSelected("gemini") },
                    modifier = Modifier.weight(1f)
                )

                ServiceButton(
                    text = "Llama",
                    isSelected = selectedService == "groq",
                    onClick = { onServiceSelected("groq") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                        stringResource(R.string.analyze_with_ai),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onTFLiteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                elevation = ButtonDefaults.buttonElevation(2.dp)
            ) {
                Text(
                    "Quick Analysis (TFLite)",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
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
                stringResource(R.string.nutrition_information),
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
                onValueChange(foodData.copy(weightInGrams = it))
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
                        onValueChange(updateField(foodData, key, newValue))
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
private fun CompositionSectionPlaceholder(
    showComposition: Boolean,
    onToggleComposition: (Boolean) -> Unit
) {
    // This will be expanded in the future to show food composition details
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "Food Composition",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Switch(
                    checked = showComposition,
                    onCheckedChange = onToggleComposition
                )
            }
            
            AnimatedVisibility(visible = showComposition) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        "Nanti mungkin nambah komposisi " +
                        "information for the analyzed food item.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "TODO: Implementation coming soon",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
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
    return ((value / 100f) * grams).toString()
}
