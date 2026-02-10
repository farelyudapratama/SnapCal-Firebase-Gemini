package com.yuch.snapcalfirebasegemini.view

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.data.api.response.NutritionData
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel
import java.util.Locale
import com.yuch.snapcalfirebasegemini.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.atan2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailFoodScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    foodId: String,
    onBack: () -> Boolean,
    viewModel: GetFoodViewModel,
) {
    val authState = authViewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val food by viewModel.food.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }

    // Auth & data fetching effects
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login") {
                popUpTo(0)
            }
            else -> Unit
        }
    }

    LaunchedEffect(foodId) {
        viewModel.fetchFoodById(foodId, true)
    }

    LaunchedEffect(navController.currentBackStackEntry) {
        val updated = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("food_updated") ?: false
        if (updated) {
            viewModel.fetchFoodById(foodId, forceRefresh = true)
            navController.currentBackStackEntry?.savedStateHandle?.set("food_updated", false)
        }
    }

    // Tampilkan SnackBar jika ada error
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    val isDeleted by viewModel.isDeleted.collectAsStateWithLifecycle()
    LaunchedEffect(isDeleted) {
        if (isDeleted) {
            navController.popBackStack()
            viewModel.clearIsDeleted()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        caloriesColor.copy(alpha = 0.8f),
                        caloriesColor.copy(alpha = 0.4f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.detail_food_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onBack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showDialog = true },
                            modifier = Modifier
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Food",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
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
                    onClick = { navController.navigate("edit-food/$foodId") },
                    containerColor = caloriesColor
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Food",
                        tint = Color.White
                    )
                }
            }
        ) { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                food?.let { foodItem ->
                    LazyColumn(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(
                                top = 56.dp,
                                bottom = paddingValues.calculateBottomPadding(),
                                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current)
                            )
                            .background(
                                color = Color(0xFFF9FAFB)
                            ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            FoodHeaderSection(foodItem, context)
                        }

                        item {
                            NutritionInfoCard(foodItem)
                        }

                        item {
                            FatBreakdownCard(foodItem.nutritionData)
                        }

                        item {
                            DateInfoCard(foodItem.createdAt ?: "N/A")
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                } ?: run {
                    // Error state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.food_details_not_available),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.delete_food)) },
            text = { Text(stringResource(R.string.delete_food_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        viewModel.deleteFood(foodId) {
                            Toast.makeText(context, R.string.delete_food_success, Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(stringResource(R.string.text_delete), color = MaterialTheme.colorScheme.error)
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

@Composable
fun FoodHeaderSection(
    foodItem: com.yuch.snapcalfirebasegemini.data.api.response.FoodItem,
    context: Context
) {
    // Check if image URL is valid
    val hasValidImage = foodItem.imageUrl?.let {
        it.isNotEmpty() && it != "null" && it != "undefined"
    } ?: false

    if (hasValidImage) {
        // Hero image with gradient overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(foodItem.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Food Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 300f,
                            endY = 900f
                        )
                    )
            )

            // Food name and meal type
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = foodItem.foodName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(4f, 4f),
                            blurRadius = 8f
                        )
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = foodItem.mealType.uppercase(Locale.getDefault()),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    } else {
        // No image: enhanced stylish header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            // Gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                caloriesColor.copy(alpha = 0.8f),
                                caloriesColor.copy(alpha = 0.4f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 1000f)
                        )
                    )
            )

            // Food icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                // Different icons based on meal type
                val icon = when(foodItem.mealType.lowercase()) {
                    "breakfast" -> Icons.Default.FreeBreakfast
                    "dinner" -> Icons.Default.DinnerDining
                    "lunch" -> Icons.Default.LunchDining
                    "snack" -> Icons.Default.LocalCafe
                    else -> Icons.Default.Restaurant
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Pattern overlay for visual interest
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val dotSize = 2.dp.toPx()
                val spacing = 20.dp.toPx()

                for (x in 0..size.width.toInt() step spacing.toInt()) {
                    for (y in 0..size.height.toInt() step spacing.toInt()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.2f),
                            radius = dotSize,
                            center = Offset(x.toFloat(), y.toFloat())
                        )
                    }
                }
            }

            // Food details
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
            ) {
                Text(
                    text = foodItem.foodName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = foodItem.mealType.uppercase(Locale.getDefault()),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Quick calorie summary below header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-16).dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Calories and Weight
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalorieCounter(foodItem.nutritionData.calories)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = foodItem.weightInGrams ?: "N/A",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "GRAMS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Vertical divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Primary macros summary
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MacroCounter("C", foodItem.nutritionData.carbs, carbsColor)
                    MacroCounter("P", foodItem.nutritionData.protein, proteinColor)
                    MacroCounter("F", foodItem.nutritionData.totalFat, fatColor)
                }
            }
        }
    }
}

@Composable
fun CalorieCounter(calories: Double) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$calories",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = caloriesColor
            )
        )
        Text(
            text = stringResource(R.string.nutrient_calories),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MacroCounter(label: String, value: Double, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$value",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}

@Composable
fun NutritionInfoCard(foodItem: com.yuch.snapcalfirebasegemini.data.api.response.FoodItem) {
    // Ngecek Apakah Image atau gambar ada
    val hasValidImage = foodItem.imageUrl?.let {
        it.isNotEmpty() && it != "null" && it != "undefined"
    } ?: false

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = if (hasValidImage) 16.dp else 8.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Heading
            Text(
                text = stringResource(R.string.nutrition_breakdown),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Hanya tampilkan judul kalori jika kita memiliki gambar (jika tidak, maka akan muncul di atas)
            if (hasValidImage) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Calories",
                            tint = caloriesColor,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.nutrient_calories),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${foodItem.nutritionData.calories}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = caloriesColor
                                )
                            )
                            Text(
                                text = "Kcal",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.weight_title),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = foodItem.weightInGrams ?: "N/A",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "Grams",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Macros with chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Chart
                NutrientDonutChart(
                    nutritionData = foodItem.nutritionData,
                    modifier = Modifier
                        .weight(1f)
                        .size(160.dp)
                )

                // Right: Macronutrient values
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    MacronutrientItem(stringResource(R.string.nutrient_carbs), "${foodItem.nutritionData.carbs}g", carbsColor)
                    MacronutrientItem(stringResource(R.string.nutrient_protein), "${foodItem.nutritionData.protein}g", proteinColor)
                    MacronutrientItem(stringResource(R.string.nutrient_fat), "${foodItem.nutritionData.totalFat}g", fatColor)
                    MacronutrientItem(stringResource(R.string.nutrient_fiber), "${foodItem.nutritionData.fiber}g", fiberColor)
                    MacronutrientItem(stringResource(R.string.nutrient_sugar), "${foodItem.nutritionData.sugar}g", sugarColor)
                }
            }
        }
    }
}

@Composable
fun FatBreakdownCard(nutritionData: NutritionData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with small icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = fatColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Fat Breakdown",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Fat breakdown visualization
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Visual representation of fat breakdown
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    // Background (total fat)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(fatColor.copy(alpha = 0.3f))
                    )

                    // Saturated fat portion
                    val totalFat = nutritionData.totalFat.toFloat()
                    val saturatedFat = nutritionData.saturatedFat.toFloat()
                    val satFatRatio = if (totalFat > 0f) saturatedFat / totalFat else 0f

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(satFatRatio)
                            .background(saturatedFatColor)
                    )

                    // Percentages
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(satFatRatio * 100).toInt()}% Saturated",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            )
                        )
                    }
                }

                // Values
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Fat:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${nutritionData.totalFat}g",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = fatColor
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Saturated Fat:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${nutritionData.saturatedFat}g",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = saturatedFatColor
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateInfoCard(createdAt: String) {
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(
        FormatStyle.MEDIUM)
    val timeFormatter = DateTimeFormatter.ofLocalizedTime(
        FormatStyle.SHORT)

    val createdAtFormatted = try {
        val zonedDateTime = Instant.parse(createdAt).atZone(
            ZoneId.systemDefault())
        "${zonedDateTime.format(dateFormatter)}, ${zonedDateTime.format(timeFormatter)}"
    } catch (e: Exception) {
        "Date not available"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Added on",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = createdAtFormatted,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
fun MacronutrientItem(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}

@Composable
fun NutrientDonutChart(
    nutritionData: NutritionData,
    modifier: Modifier = Modifier
) {
    val total = nutritionData.carbs + nutritionData.protein + nutritionData.totalFat + nutritionData.fiber + nutritionData.sugar

    if (total <= 0) return // Jangan tampilkan chart jika tidak ada data

    val colors = listOf(carbsColor, proteinColor, fatColor, fiberColor, sugarColor)
    val nutrientNames = listOf("Carbohydrates", "Protein", "Fat", "Fiber", "Sugar")

    val nutrientValues = listOf(
        nutritionData.carbs,
        nutritionData.protein,
        nutritionData.totalFat,
        nutritionData.fiber,
        nutritionData.sugar
    )

    val minAngle = 5f
    var angles = nutrientValues.map { maxOf(360.0 * (it / total), minAngle.toDouble()) }
    val correctionFactor = 360f / angles.sum()
    angles = angles.map { it * correctionFactor }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val center = Offset(
                            (size.width / 2).toFloat(),
                            (size.height / 2).toFloat()
                        )
                        val touchAngle = (atan2(
                            tapOffset.y - center.y,
                            tapOffset.x - center.x
                        ) * (180 / Math.PI)).toFloat()

                        val normalizedAngle = (touchAngle + 360) % 360
                        var startAngle = -90f
                        angles.forEachIndexed { index, angle ->
                            if (normalizedAngle in startAngle..(startAngle + angle.toFloat())) {
                                if (selectedIndex != index) {
                                    haptic.performHapticFeedback(
                                        HapticFeedbackType.TextHandleMove)
                                }
                                selectedIndex = index
                                return@detectTapGestures
                            }
                            startAngle += angle.toFloat()
                        }
                        // Jika tap di luar area slice, reset
                        if (selectedIndex != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        selectedIndex = null
                    }
                }
        ) {
            val strokeWidth = size.width * 0.2f
            val radius = (minOf(size.width, size.height) - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            var startAngle = -90f

            angles.zip(colors).forEach { (angle, color) ->
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = angle.toFloat(),
                    useCenter = false,
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    topLeft = Offset(center.x - radius, center.y - radius)
                )
                startAngle += angle.toFloat()
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = selectedIndex?.let {
                    val percentage = (nutrientValues[it] / total) * 100
                    "%.1f%%".format(percentage)
                } ?: "${nutritionData.calories}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = selectedIndex?.let { colors[it] } ?: caloriesColor
                )
            )
            Text(
                text = selectedIndex?.let { nutrientNames[it] } ?: "Calories",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
