package com.yuch.snapcalfirebasegemini.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuch.snapcalfirebasegemini.data.model.EditableFoodData
import com.yuch.snapcalfirebasegemini.viewmodel.FoodViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    modifier: Modifier = Modifier,
    viewModel: FoodViewModel = viewModel(),
    onBack: () -> Unit,
    onImageCapture: () -> Unit
) {
    var foodData by remember {
        mutableStateOf(
            EditableFoodData(
                foodName = "",
                calories = "",
                carbs = "",
                protein = "",
                totalFat = "",
                saturatedFat = "",
                fiber = "",
                sugar = "",
                mealType = null
            )
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    // Handle error messages
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
                        "Manual Food Entry",
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
            FloatingActionButton(
                onClick = {
                    if (foodData.mealType != null && foodData.foodName.isNotBlank()) {
                        viewModel.uploadFood(null, foodData)
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Please fill in food name and select meal type",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = modifier.padding(16.dp)
                    ) {
                        // Optional Image Button
                        OutlinedButton(
                            onClick = onImageCapture,
                            modifier = modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = "Add Photo",
                                    modifier = modifier.size(48.dp)
                                )
                                Spacer(modifier = modifier.height(8.dp))
                                Text("Add Food Photo (Optional)")
                            }
                        }

                        Spacer(modifier = modifier.height(16.dp))

                        // Meal Type Dropdown
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
                                modifier = modifier
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
                                            foodData = foodData.copy(mealType = mealType)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = modifier.height(16.dp))

                        // Food Name
                        OutlinedTextField(
                            value = foodData.foodName,
                            onValueChange = { foodData = foodData.copy(foodName = it) },
                            label = { Text("Food Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nutrition Fields
                        EditableNutritionRow("Calories (kcal)", foodData.calories) {
                            foodData = foodData.copy(calories = it)
                        }

                        EditableNutritionRow("Carbs (g)", foodData.carbs) {
                            foodData = foodData.copy(carbs = it)
                        }

                        EditableNutritionRow("Protein (g)", foodData.protein) {
                            foodData = foodData.copy(protein = it)
                        }

                        EditableNutritionRow("Total Fat (g)", foodData.totalFat) {
                            foodData = foodData.copy(totalFat = it)
                        }

                        EditableNutritionRow("Saturated Fat (g)", foodData.saturatedFat) {
                            foodData = foodData.copy(saturatedFat = it)
                        }

                        EditableNutritionRow("Fiber (g)", foodData.fiber) {
                            foodData = foodData.copy(fiber = it)
                        }

                        EditableNutritionRow("Sugar (g)", foodData.sugar) {
                            foodData = foodData.copy(sugar = it)
                        }
                    }
                }
            }

            // Spacer at bottom to avoid FAB overlap
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
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