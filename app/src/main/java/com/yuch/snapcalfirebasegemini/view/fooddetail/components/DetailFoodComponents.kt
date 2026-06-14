package com.yuch.snapcalfirebasegemini.view.fooddetail.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.api.response.NutritionData
import com.yuch.snapcalfirebasegemini.ui.theme.caloriesColor
import com.yuch.snapcalfirebasegemini.ui.theme.carbsColor
import com.yuch.snapcalfirebasegemini.ui.theme.fatColor
import com.yuch.snapcalfirebasegemini.ui.theme.fiberColor
import com.yuch.snapcalfirebasegemini.ui.theme.proteinColor
import com.yuch.snapcalfirebasegemini.ui.theme.saturatedFatColor
import com.yuch.snapcalfirebasegemini.ui.theme.sugarColor
import com.yuch.snapcalfirebasegemini.ui.utils.toReadableNutritionSource
import com.yuch.snapcalfirebasegemini.utils.formatCreatedAtForDisplay
import java.util.Locale
import kotlin.math.atan2

@Composable
fun DeleteFoodDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_food)) },
        text = { Text(stringResource(R.string.delete_food_confirmation)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.text_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.text_cancel))
            }
        }
    )
}

@Composable
fun FoodDetailEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
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

@Composable
fun FoodHeaderSection(foodItem: FoodItem, context: Context) {
    val hasValidImage = foodItem.imageUrl?.let {
        it.isNotEmpty() && it != "null" && it != "undefined"
    } ?: false

    if (hasValidImage) {
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

                MealTypeBadge(
                    text = foodItem.mealType.uppercase(Locale.getDefault()),
                    backgroundColor = MaterialTheme.colorScheme.secondary
                )
            }
        }
    } else {
        FoodHeaderWithoutImage(foodItem)
    }
}

@Composable
fun NutritionSourceCard(nutritionData: NutritionData) {
    val sourceType = nutritionData.sourceType ?: "manual"
    val sourceDetails = nutritionData.sourceDetails

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nutrition source: ${sourceType.toReadableNutritionSource()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            sourceDetails?.estimatedBy?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Estimated by: $it", style = MaterialTheme.typography.bodySmall)
            }
            sourceDetails?.basis?.let {
                Text("Basis: $it", style = MaterialTheme.typography.bodySmall)
            }
            sourceDetails?.confidenceNote?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            if (sourceDetails?.generalReferences?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "References: ${sourceDetails.generalReferences.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
            }
            if (sourceDetails?.requiresUserVerification == true) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Values are AI estimates and should be verified.",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun FoodHeaderWithoutImage(foodItem: FoodItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
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

        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (foodItem.mealType.lowercase()) {
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

        Canvas(modifier = Modifier.fillMaxSize()) {
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

            MealTypeBadge(
                text = foodItem.mealType.uppercase(Locale.getDefault()),
                backgroundColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }

    QuickCalorieSummary(foodItem)
}

@Composable
private fun MealTypeBadge(text: String, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun QuickCalorieSummary(foodItem: FoodItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-16).dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CalorieCounter(foodItem.nutritionData.calories)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

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

@Composable
fun CalorieCounter(calories: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
fun NutritionInfoCard(foodItem: FoodItem) {
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.nutrition_breakdown),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NutrientDonutChart(
                    nutritionData = foodItem.nutritionData,
                    modifier = Modifier
                        .weight(1f)
                        .size(160.dp)
                )

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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(fatColor.copy(alpha = 0.3f))
                    )

                    val totalFat = nutritionData.totalFat.toFloat()
                    val saturatedFat = nutritionData.saturatedFat.toFloat()
                    val satFatRatio = if (totalFat > 0f) saturatedFat / totalFat else 0f

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(satFatRatio)
                            .background(saturatedFatColor)
                    )

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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    FatValueRow("Total Fat:", "${nutritionData.totalFat}g", fatColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    FatValueRow("Saturated Fat:", "${nutritionData.saturatedFat}g", saturatedFatColor)
                }
            }
        }
    }
}

@Composable
private fun FatValueRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
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
fun DateInfoCard(createdAt: String) {
    val createdAtFormatted = formatCreatedAtForDisplay(createdAt) ?: "Date not available"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
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
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
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

    if (total <= 0) return

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
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                selectedIndex = index
                                return@detectTapGestures
                            }
                            startAngle += angle.toFloat()
                        }
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

fun showDeleteFoodToast(context: Context) {
    Toast.makeText(context, R.string.delete_food_success, Toast.LENGTH_SHORT).show()
}
