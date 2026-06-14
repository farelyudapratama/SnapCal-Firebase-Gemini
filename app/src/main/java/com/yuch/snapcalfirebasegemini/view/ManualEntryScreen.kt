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
import com.yuch.snapcalfirebasegemini.utils.uriToFile
import com.yuch.snapcalfirebasegemini.viewmodel.FoodEntryViewModel
import kotlinx.coroutines.launch
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.ui.components.ImagePermissionHandler
import com.yuch.snapcalfirebasegemini.ui.components.food.FoodTextField
import com.yuch.snapcalfirebasegemini.ui.components.food.FoodImagePickerPreview
import com.yuch.snapcalfirebasegemini.ui.components.food.MealTypeDropdown
import com.yuch.snapcalfirebasegemini.ui.components.food.NutritionFields

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: FoodEntryViewModel,
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
                weightInGrams = ""
            )
        )
    }
    val focusManager = LocalFocusManager.current
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
                context.getString(R.string.food_entry_added_successfully),
                Toast.LENGTH_SHORT
            ).show()

            viewModel.resetState()

            // Pop sampai ke List screen
            onSuccessfulUpload()
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
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.add_food_entry),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                        if (foodData.mealType != null && foodData.foodName.isNotBlank()) {
                            viewModel.uploadFood(
                                selectedImageUri?.let { uriToFile(context, it).absolutePath },
                                foodData
                            )

                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.please_fill_in_food_name_and_select_meal_type),
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
                    .padding(
                        top = 28.dp,
                        bottom = paddingValues.calculateBottomPadding(),
                        start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                        end = paddingValues.calculateEndPadding(LocalLayoutDirection.current)
                    )
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
                                FoodImagePickerPreview(
                                    selectedImageUri = selectedImageUri,
                                    onSelectImage = {
                                        if (hasPermission) {
                                            galleryLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        } else {
                                            showPermissionDialog()
                                        }
                                    },
                                    enabled = hasPermission,
                                    emptyText = "Tap to selected Image",
                                    selectedContentDescription = "Selected Image",
                                    changeText = "Tap to change photo"
                                )
                            }

                            Spacer(modifier = modifier.height(24.dp))

                            Text(
                                stringResource(R.string.food_information),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Meal Type Selector
                            MealTypeDropdown(
                                selectedMealType = foodData.mealType,
                                onMealTypeSelected = { foodData = foodData.copy(mealType = it) }
                            )

                            // Food Name Input
                            OutlinedTextField(
                                value = foodData.foodName,
                                onValueChange = { foodData = foodData.copy(foodName = it) },
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

                            // Weight Input
                            FoodTextField(
                                value = foodData.weightInGrams,
                                onValueChange = { foodData = foodData.copy(weightInGrams = it) },
                                label = stringResource(R.string.weight_g),
                                leadingIcon = { Icon(Icons.Default.Restaurant, "Weight") },
                                onNext = { focusManager.clearFocus() },
                                keyboardType = KeyboardType.Number
                            )

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

                            NutritionFields(
                                calories = foodData.calories,
                                carbs = foodData.carbs,
                                protein = foodData.protein,
                                totalFat = foodData.totalFat,
                                saturatedFat = foodData.saturatedFat,
                                fiber = foodData.fiber,
                                sugar = foodData.sugar,
                                onCaloriesChange = { foodData = foodData.copy(calories = it) },
                                onCarbsChange = { foodData = foodData.copy(carbs = it) },
                                onProteinChange = { foodData = foodData.copy(protein = it) },
                                onTotalFatChange = { foodData = foodData.copy(totalFat = it) },
                                onSaturatedFatChange = { foodData = foodData.copy(saturatedFat = it) },
                                onFiberChange = { foodData = foodData.copy(fiber = it) },
                                onSugarChange = { foodData = foodData.copy(sugar = it) },
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
