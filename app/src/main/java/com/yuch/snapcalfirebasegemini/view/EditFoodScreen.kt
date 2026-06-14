package com.yuch.snapcalfirebasegemini.view

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.model.UpdateFoodData
import com.yuch.snapcalfirebasegemini.ui.components.food.FoodImagePickerPreview
import com.yuch.snapcalfirebasegemini.ui.components.food.FoodTextField
import com.yuch.snapcalfirebasegemini.ui.components.food.MealTypeDropdown
import com.yuch.snapcalfirebasegemini.ui.components.food.NutritionFields
import com.yuch.snapcalfirebasegemini.utils.uriToFile
import com.yuch.snapcalfirebasegemini.viewmodel.FoodDetailViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.FoodEntryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFoodScreen(
    foodId: String,
    navController: NavController,
    foodItem: FoodItem?, // Data makanan yang akan diedit harusnya
    onUpdateFood: (String, String?, UpdateFoodData) -> Unit,
    onBack: () -> Unit,
    detailViewModel: FoodDetailViewModel,
    foodViewModel: FoodEntryViewModel
) {
    var foodName by remember { mutableStateOf(foodItem?.foodName ?: "") }
    var weightInGrams by remember { mutableStateOf(foodItem?.weightInGrams ?: "") }
    var mealType by remember { mutableStateOf(foodItem?.mealType ?: "") }
    var calories by remember { mutableStateOf(foodItem?.nutritionData?.calories?.toString() ?: "") }
    var carbs by remember { mutableStateOf(foodItem?.nutritionData?.carbs?.toString() ?: "") }
    var protein by remember { mutableStateOf(foodItem?.nutritionData?.protein?.toString() ?: "") }
    var totalFat by remember { mutableStateOf(foodItem?.nutritionData?.totalFat?.toString() ?: "") }
    var saturatedFat by remember { mutableStateOf(foodItem?.nutritionData?.saturatedFat?.toString() ?: "") }
    var fiber by remember { mutableStateOf(foodItem?.nutritionData?.fiber?.toString() ?: "") }
    var sugar by remember { mutableStateOf(foodItem?.nutritionData?.sugar?.toString() ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val imageDeletedMessage by detailViewModel.imageDeletedMessage.collectAsStateWithLifecycle()

    // Focus controllers
    val focusManager = LocalFocusManager.current
    val foodNameFocus = remember { FocusRequester() }
    val caloriesFocus = remember { FocusRequester() }
    val carbsFocus = remember { FocusRequester() }
    val proteinFocus = remember { FocusRequester() }
    val totalFatFocus = remember { FocusRequester() }
    val saturatedFatFocus = remember { FocusRequester() }
    val fiberFocus = remember { FocusRequester() }
    val sugarFocus = remember { FocusRequester() }

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
        }
    }

    val isLoading by foodViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by foodViewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by foodViewModel.successMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Tangani perubahan errorMessage
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                foodViewModel.clearErrorMessage()
            }
        }
    }

    // Tangani perubahan successMessage
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar("$message (Redirecting in 2 seconds)")
                foodViewModel.clearErrorMessage()
                delay(2000)
                navController.previousBackStackEntry?.savedStateHandle?.set("food_updated", true)
                navController.popBackStack()
            }
        }
    }

    LaunchedEffect(imageDeletedMessage) {
        imageDeletedMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                detailViewModel.clearImageDeletedMessage()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    ),
                    endY = 400f
                )
            )
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Edit",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        try {
                            val foodData = try {
                                UpdateFoodData(
                                    foodName = foodName,
                                    mealType = mealType,
                                    weightInGrams = weightInGrams,
                                    calories = calories.toDoubleOrNull() ?: 0.0,
                                    carbs = carbs.toDoubleOrNull() ?: 0.0,
                                    protein = protein.toDoubleOrNull() ?: 0.0,
                                    totalFat = totalFat.toDoubleOrNull() ?: 0.0,
                                    saturatedFat = saturatedFat.toDoubleOrNull() ?: 0.0,
                                    fiber = fiber.toDoubleOrNull() ?: 0.0,
                                    sugar = sugar.toDoubleOrNull() ?: 0.0,
                                    sourceType = foodItem?.nutritionData?.sourceType,
                                    sourceDetails = foodItem?.nutritionData?.sourceDetails
                                )
                            } catch (e: Exception) {
                                Log.e("EditFoodScreen", "Error parsing food data: ${e.message}")
                                return@FloatingActionButton
                            }

                            onUpdateFood(
                                foodId,
                                selectedImageUri?.let { uriToFile(context, it).absolutePath },
                                foodData
                            )
                        } catch (e: Exception) {
                            Log.e("EditFoodScreen", "Error updating food: ${e.message}")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save Changes")
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 88.dp,
                        bottom = padding.calculateBottomPadding(),
                        start = padding.calculateStartPadding(LocalLayoutDirection.current),
                        end = padding.calculateEndPadding(LocalLayoutDirection.current)
                    )
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            // Image Selection with Preview
                            FoodImagePickerPreview(
                                selectedImageUri = selectedImageUri,
                                currentImageUrl = foodItem?.imageUrl,
                                onSelectImage = {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                emptyText = stringResource(R.string.selected_food_image),
                                selectedContentDescription = stringResource(R.string.selected_food_image),
                                changeText = stringResource(R.string.tap_to_change_photo)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Image actions (Change/Delete buttons)
                            ImageActionButtons(
                                hasExistingImage = foodItem?.imageUrl != null || selectedImageUri != null,
                                canDeleteImage = foodItem?.imageUrl != null,
                                onSelectImage = {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onDeleteImage = {
                                    detailViewModel.deleteFoodImageById(foodId)
                                }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Meal Type Selection
                            MealTypeDropdown(
                                selectedMealType = mealType,
                                onMealTypeSelected = { mealType = it },
                                showLabelAbove = true
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                stringResource(R.string.food_information),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Weight Input
                            FoodTextField(
                                value = weightInGrams,
                                onValueChange = { weightInGrams = it },
                                label = stringResource(R.string.weight_g),
                                leadingIcon = { Icon(Icons.Default.Restaurant, "Weight") },
                                onNext = { focusManager.clearFocus() },
                                keyboardType = KeyboardType.Number
                            )

                            // Food Name Input
                            FoodTextField(
                                value = foodName,
                                onValueChange = { foodName = it },
                                label = stringResource(R.string.food_name),
                                leadingIcon = { Icon(Icons.Default.Restaurant, "Food") },
                                focusRequester = foodNameFocus,
                                onNext = { caloriesFocus.requestFocus() }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Nutrition Section
                            Text(
                                stringResource(R.string.nutrition_values),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            NutritionFields(
                                calories = calories,
                                carbs = carbs,
                                protein = protein,
                                totalFat = totalFat,
                                saturatedFat = saturatedFat,
                                fiber = fiber,
                                sugar = sugar,
                                onCaloriesChange = { calories = it },
                                onCarbsChange = { carbs = it },
                                onProteinChange = { protein = it },
                                onTotalFatChange = { totalFat = it },
                                onSaturatedFatChange = { saturatedFat = it },
                                onFiberChange = { fiber = it },
                                onSugarChange = { sugar = it },
                                caloriesFocus = caloriesFocus,
                                carbsFocus = carbsFocus,
                                proteinFocus = proteinFocus,
                                totalFatFocus = totalFatFocus,
                                saturatedFatFocus = saturatedFatFocus,
                                fiberFocus = fiberFocus,
                                sugarFocus = sugarFocus,
                                onDone = { focusManager.clearFocus() }
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
}

@Composable
private fun ImageActionButtons(
    hasExistingImage: Boolean,
    canDeleteImage: Boolean,
    onSelectImage: () -> Unit,
    onDeleteImage: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        val buttonText = if (hasExistingImage) stringResource(R.string.tap_to_change_photo) else stringResource(R.string.selected_food_image)
        OutlinedButton(onClick = onSelectImage) {
            Text(buttonText)
        }

        Spacer(modifier = Modifier.width(8.dp))

        var showDialog by remember { mutableStateOf(false) }
        OutlinedButton(
            onClick = { showDialog = true },
            enabled = canDeleteImage,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Red,
                disabledContentColor = Color.Gray
            ),
            border = BorderStroke(1.dp, if (canDeleteImage) Color.Red else Color.Gray)
        ) {
            Text(stringResource(R.string.delete_image))
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(R.string.delete_image)) },
                text = { Text(stringResource(R.string.are_you_sure_you_want_to_delete_this_image_this_action_cannot_be_undone)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteImage()
                            showDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.text_delete), color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(R.string.text_cancel))
                    }
                }
            )
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
