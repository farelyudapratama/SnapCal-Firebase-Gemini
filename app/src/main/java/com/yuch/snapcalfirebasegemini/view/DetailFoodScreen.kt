package com.yuch.snapcalfirebasegemini.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.api.response.NutritionData
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel
import java.util.Locale

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
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val food by viewModel.food.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login") {
                popUpTo(0)
            }
            else -> Unit
        }
    }

    LaunchedEffect(foodId) {
        viewModel.fetchFoodById(foodId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Food Details",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("edit-food/$foodId") }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Food")
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
                CircularProgressIndicator()
            }
        } else {
            food?.let { foodItem ->
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
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

                            // Gradient overlay at the bottom
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

                            // Food name at the bottom
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
                                ),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            )

                        }
                    } else {
                        // Show donut chart instead of image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Donut Chart
                                MacronutrientDonutChart(
                                    nutritionData = foodItem.nutritionData,
                                    modifier = Modifier
                                        .size(180.dp)
                                        .padding(8.dp)
                                )

                                // Food name below chart
                                Text(
                                    text = foodItem.foodName,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    // Meal type badge
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = foodItem.mealType.uppercase(Locale.getDefault()),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Content section
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Info row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoItem(
                                icon = Icons.Default.LocalFireDepartment,
                                label = "Calories",
                                value = "${foodItem.nutritionData.calories} kcal",
                                tint = MaterialTheme.colorScheme.error
                            )

                            InfoItem(
                                icon = Icons.Default.Schedule,
                                label = "Added on",
                                value = foodItem.createdAt
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Nutrition section title
                        Text(
                            text = "Nutrition Information",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Nutrition cards
                        NutritionCards(nutritionData = foodItem.nutritionData)
                    }
                }
            } ?: run {
                // Show error or empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Food details not available",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun MacronutrientDonutChart(
    nutritionData: NutritionData,
    modifier: Modifier = Modifier
) {
    // Calculate the total of macronutrients for percentages
    val carbsGrams = nutritionData.carbs
    val proteinGrams = nutritionData.protein
    val fatGrams = nutritionData.totalFat
    val total = carbsGrams + proteinGrams + fatGrams

    // Colors for each macronutrient
    val carbsColor = MaterialTheme.colorScheme.primary
    val proteinColor = MaterialTheme.colorScheme.tertiary
    val fatColor = MaterialTheme.colorScheme.secondary

    // Calculate angles
    val carbsAngle = 360f * (carbsGrams / total)
    val proteinAngle = 360f * (proteinGrams / total)
    val fatAngle = 360f * (fatGrams / total)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Draw the donut chart
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.width * 0.15f
            val radius = (size.minDimension - strokeWidth) / 2
            val innerRadius = radius * 0.6f // This creates the donut hole
            val centerX = size.width / 2
            val centerY = size.height / 2

            var startAngle = -90f // Start at top

            // Draw carbs arc
            if (carbsGrams > 0) {
                drawArc(
                    color = carbsColor,
                    startAngle = startAngle,
                    sweepAngle = carbsAngle.toFloat(),
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += carbsAngle.toFloat()
            }

            // Draw protein arc
            if (proteinGrams > 0) {
                drawArc(
                    color = proteinColor,
                    startAngle = startAngle,
                    sweepAngle = proteinAngle.toFloat(),
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += proteinAngle.toFloat()
            }

            // Draw fat arc
            if (fatGrams > 0) {
                drawArc(
                    color = fatColor,
                    startAngle = startAngle,
                    sweepAngle = fatAngle.toFloat(),
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
            }
        }

        // Display calories in the center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${nutritionData.calories}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "kcal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Legend
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(color = carbsColor, name = "Carbs", value = "${carbsGrams}g")
        LegendItem(color = proteinColor, name = "Protein", value = "${proteinGrams}g")
        LegendItem(color = fatColor, name = "Fats", value = "${fatGrams}g")
    }
}

@Composable
fun LegendItem(color: Color, name: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Text(
            text = "$name: $value",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
fun NutritionCards(nutritionData: NutritionData) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Macronutrients row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NutrientCard(
                name = "Carbs",
                value = "${nutritionData.carbs}g",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            NutrientCard(
                name = "Protein",
                value = "${nutritionData.protein}g",
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            NutrientCard(
                name = "Fats",
                value = "${nutritionData.totalFat}g",
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detailed nutrition table
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Detailed Nutrition",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                NutrientRow("Total Fat", "${nutritionData.totalFat}g")
                NutrientRow("Saturated Fat", "${nutritionData.saturatedFat}g")
                NutrientRow("Carbohydrates", "${nutritionData.carbs}g")
                NutrientRow("Fiber", "${nutritionData.fiber}g")
                NutrientRow("Sugar", "${nutritionData.sugar}g")
                NutrientRow("Protein", "${nutritionData.protein}g")
            }
        }
    }
}

@Composable
fun NutrientCard(
    name: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Colored circle indicator
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NutrientRow(
    label: String,
    value: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    }
}