package com.yuch.snapcalfirebasegemini.view

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.yuch.snapcalfirebasegemini.data.model.EditableFoodData
import com.yuch.snapcalfirebasegemini.viewmodel.FoodViewModel
import kotlinx.coroutines.launch
import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.yuch.snapcalfirebasegemini.ui.components.ImagePermissionHandler
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: FoodViewModel,
    onSuccessfulUpload: () -> Boolean
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
                mealType = null,
                weightInGrams = "100"
            )
        )
    }
    val focusManager = LocalFocusManager.current
    val foodNameFocus = remember { FocusRequester() }
    val caloriesFocus = remember { FocusRequester() }
    val carbsFocus = remember { FocusRequester() }
    val proteinFocus = remember { FocusRequester() }
    val totalFatFocus = remember { FocusRequester() }
    val saturatedFatFocus = remember { FocusRequester() }
    val fiberFocus = remember { FocusRequester() }
    val sugarFocus = remember { FocusRequester() }

    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImageUri = it

            // Handle selected image
            val file = uriToFile(context, it)
            viewModel.uploadFood(file.absolutePath, foodData)
        }
    }

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

    val uploadSuccess by viewModel.uploadSuccess.collectAsStateWithLifecycle()

    // Handle navigation on success
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Food Entry",
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
                        viewModel.uploadFood(
                            selectedImageUri?.let { uriToFile(context, it).absolutePath },
                            foodData
                        )

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
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = modifier.padding(24.dp)
                    ) {
                        // Image Selection with Preview
                        ImagePermissionHandler(
                            onPermissionGranted = { hasPermission = true }
                        ) { showPermissionDialog ->
                            ImageSelectionButton(
                                selectedImageUri = selectedImageUri,
                                hasPermission = hasPermission,
                                onImageSelect = {
                                    if (hasPermission) {
                                        galleryLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    } else {
                                        showPermissionDialog()
                                    }
                                }
                            )
                        }


                        Spacer(modifier = modifier.height(24.dp))

                        // Meal Type Selector
                        MealTypeDropdown(
                            selectedMealType = foodData.mealType,
                            onMealTypeSelected = { foodData = foodData.copy(mealType = it) }
                        )

                        Spacer(modifier = modifier.height(24.dp))

                        Text(
                            "Food Information",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Food Name Input
                        TextField(
                            value = foodData.foodName,
                            onValueChange = { foodData = foodData.copy(foodName = it) },
                            label = "Food Name",
                            leadingIcon = { Icon(Icons.Default.Restaurant, "Food") },
                            focusRequester = foodNameFocus,
                            onNext = { caloriesFocus.requestFocus() }
                        )

                        // Weight Input
                        TextField(
                            value = foodData.weightInGrams,
                            onValueChange = { foodData = foodData.copy(weightInGrams = it) },
                            label = "Weight (g)",
                            leadingIcon = { Icon(Icons.Default.Restaurant, "Weight") },
                            focusRequester = FocusRequester(),
                            onNext = { focusManager.clearFocus() },
                            keyboardType = KeyboardType.Number
                        )

                        Spacer(modifier = modifier.height(24.dp))

                        // Nutrition Section
                        // Nutrition Inputs
                        NutritionField(
                            label = "Calories",
                            value = foodData.calories,
                            onValueChange = { foodData = foodData.copy(calories = it) },
                            focusRequester = caloriesFocus,
                            onNext = { carbsFocus.requestFocus() },
                            icon = Icons.Default.LocalFireDepartment
                        )

                        NutritionField(
                            label = "Carbohydrates (g)",
                            value = foodData.carbs,
                            onValueChange = { foodData = foodData.copy(carbs = it) },
                            focusRequester = carbsFocus,
                            onNext = { proteinFocus.requestFocus() },
                            icon = Icons.Default.Grain
                        )

                        NutritionField(
                            label = "Protein (g)",
                            value = foodData.protein,
                            onValueChange = { foodData = foodData.copy(protein = it) },
                            focusRequester = proteinFocus,
                            onNext = { totalFatFocus.requestFocus() },
                            icon = Icons.Default.FitnessCenter
                        )

                        NutritionField(
                            label = "Total Fat (g)",
                            value = foodData.totalFat,
                            onValueChange = { foodData = foodData.copy(totalFat = it) },
                            focusRequester = totalFatFocus,
                            onNext = { saturatedFatFocus.requestFocus() },
                            icon = Icons.Default.WaterDrop
                        )

                        NutritionField(
                            label = "Saturated Fat (g)",
                            value = foodData.saturatedFat,
                            onValueChange = { foodData = foodData.copy(saturatedFat = it) },
                            focusRequester = saturatedFatFocus,
                            onNext = { fiberFocus.requestFocus() },
                            icon = Icons.Default.WaterDrop
                        )

                        NutritionField(
                            label = "Fiber (g)",
                            value = foodData.fiber,
                            onValueChange = { foodData = foodData.copy(fiber = it) },
                            focusRequester = fiberFocus,
                            onNext = { sugarFocus.requestFocus() },
                            icon = Icons.Default.Grass
                        )

                        NutritionField(
                            label = "Sugar (g)",
                            value = foodData.sugar,
                            onValueChange = { foodData = foodData.copy(sugar = it) },
                            focusRequester = sugarFocus,
                            onNext = { focusManager.clearFocus() },
                            icon = Icons.Default.Cookie
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        if (isLoading) {
            LoadingOverlay()
        }
    }
}

@Composable
private fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    focusRequester: FocusRequester,
    onNext: () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNext() }
        ),
        singleLine = true
    )
}

@Composable
private fun NutritionField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onNext: () -> Unit,
    icon: ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Only allow numeric input
            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .focusRequester(focusRequester),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNext() }
        ),
        singleLine = true
    )
}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
private fun MealTypeDropdown(
    selectedMealType: String?,
    onMealTypeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val mealTypes = listOf(
        "breakfast" to Icons.Default.BreakfastDining,
        "lunch" to Icons.Default.LunchDining,
        "dinner" to Icons.Default.DinnerDining,
        "snack" to Icons.Default.Icecream,
        "drink" to Icons.Default.LocalCafe
    )

    Column {
        Text(
            "Meal Type",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedMealType?.replaceFirstChar { it.uppercase() } ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                ),
                placeholder = { Text("Select meal type") }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                mealTypes.forEach { (type, icon) ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(type.replaceFirstChar { it.uppercase() })
                            }
                        },
                        onClick = {
                            onMealTypeSelected(type)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Saving food entry...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun ImageSelectionButton(
    selectedImageUri: Uri?,
    hasPermission: Boolean,
    onImageSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = hasPermission) { onImageSelect() },
        contentAlignment = Alignment.Center
    ) {
        if (selectedImageUri != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = "Selected Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "Tap to change photo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .offset(y = (15).dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Selected Image",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to selected Image",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun uriToFile(context: Context, uri: Uri): File {
    val contentResolver = context.contentResolver
    val file = kotlin.io.path.createTempFile(suffix = ".jpg").toFile()

    contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }

    return file
}