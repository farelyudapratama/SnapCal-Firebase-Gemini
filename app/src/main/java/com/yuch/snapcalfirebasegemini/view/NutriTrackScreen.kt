package com.yuch.snapcalfirebasegemini.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Data models tetap sama
data class MealEntry(
    val id: Int,
    val type: String,
    val description: String,
    val calories: Int,
    val time: String,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

data class NutritionSummary(
    val calories: Int,
    val caloriesGoal: Int,
    val protein: Int,
    val proteinGoal: Int,
    val carbs: Int,
    val carbsGoal: Int,
    val fat: Int,
    val fatGoal: Int,
    val fiber: Int,
    val fiberGoal: Int
)

data class WeeklyProgress(
    val avgCalories: Int,
    val weightChange: Float,
    val workoutCount: Int,
    val waterAverage: Float
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NutriTrackScreen(navController: NavController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login") { popUpTo(0) }
            else -> Unit
        }
    }

    // State variables
    // Dummy data
    val waterGlasses = remember { mutableStateOf(6) }

    val nutritionSummary = NutritionSummary(
        calories = 1245,
        caloriesGoal = 2000,
        protein = 95,
        proteinGoal = 120,
        carbs = 100,
        carbsGoal = 250,
        fat = 55,
        fatGoal = 70,
        fiber = 28,
        fiberGoal = 35
    )

    val weeklyProgress = WeeklyProgress(
        avgCalories = 1560,
        weightChange = -0.5f,
        workoutCount = 4,
        waterAverage = 7.2f
    )

    val mealEntries = remember {
        mutableStateListOf(
            MealEntry(
                id = 1,
                type = "Breakfast",
                description = "Oatmeal with Berries",
                calories = 320,
                time = "8:30 AM",
                protein = 12,
                carbs = 45,
                fat = 8
            ),
            MealEntry(
                id = 2,
                type = "Lunch",
                description = "Chicken Salad",
                calories = 450,
                time = "12:45 PM",
                protein = 38,
                carbs = 25,
                fat = 22
            ),
            MealEntry(
                id = 3,
                type = "Dinner",
                description = "Salmon with Vegetables",
                calories = 475,
                time = "7:15 PM",
                protein = 45,
                carbs = 30,
                fat = 25
            )
        )
    }

    val exerciseCaloriesBurned = 320

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "NutriTrack",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            item { HeaderSection() }

            item {
                SummaryCardsRow(
                    nutritionSummary = nutritionSummary,
                    weeklyProgress = weeklyProgress
                )
            }

            item {
                SectionHeader(
                    title = "Meal Tracking - Today",
                    buttonText = "Add Meal",
                    icon = Icons.Default.Add,
                    onClick = { /* Add meal logic */ }
                )
            }

            items(mealEntries, key = { it.id }) { meal ->
                MealEntryCard(
                    meal = meal,
                    onDelete = { /* Delete logic */ },
                    modifier = Modifier.animateItemPlacement()
                )
            }

            item {
                NutritionBreakdownSection(nutritionSummary = nutritionSummary)
            }

            item {
                HydrationExerciseRow(
                    waterGlasses = waterGlasses,
                    exerciseCalories = exerciseCaloriesBurned
                )
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Daily Nutrition Overview",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun SummaryCardsRow(
    nutritionSummary: NutritionSummary,
    weeklyProgress: WeeklyProgress
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Goals Card
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "Daily Goals",
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            NutrientProgress(
                label = "Calories",
                current = nutritionSummary.calories,
                goal = nutritionSummary.caloriesGoal,
                color = MaterialTheme.colorScheme.primary,
                unit = "kcal"
            )
            NutrientProgress(
                label = "Protein",
                current = nutritionSummary.protein,
                goal = nutritionSummary.proteinGoal,
                color = Color(0xFF00C853),
                unit = "g"
            )
        }

        // Weekly Progress Card
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "Weekly Progress",
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            WeeklyProgressItem(
                label = "Avg. Calories",
                value = "${weeklyProgress.avgCalories} kcal",
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            WeeklyProgressItem(
                label = "Weight Change",
                value = "${weeklyProgress.weightChange}kg",
                color = if (weeklyProgress.weightChange < 0) Color(0xFF00C853) else Color.Red
            )
            WeeklyProgressItem(
                label = "Workouts",
                value = weeklyProgress.workoutCount.toString(),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    containerColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        content()
    }
    }
}

@Composable
private fun NutrientProgress(
    label: String,
    current: Int,
    goal: Int,
    color: Color,
    unit: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("$current/$goal $unit", style = MaterialTheme.typography.bodyMedium)
        }
        LinearProgressIndicator(
            progress = current.toFloat() / goal,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun WeeklyProgressItem(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

@Composable
private fun SectionHeader(
    title: String,
    buttonText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

        FilledTonalButton(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(buttonText)
        }
    }
}

@Composable
private fun MealEntryCard(
    meal: MealEntry,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.padding(vertical = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MealTypeChip(type = meal.type)
                Spacer(Modifier.weight(1f))
                Text(
                    meal.time,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error)
                }
            }

            Text(
                meal.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            NutrientGrid(
                calories = meal.calories,
                protein = meal.protein,
                carbs = meal.carbs,
                fat = meal.fat
            )
        }
    }
}

@Composable
private fun MealTypeChip(type: String) {
    val (color, emoji) = when (type.toLowerCase()) {
        "breakfast" -> Pair(Color(0xFFFFF176), "☕")
        "lunch" -> Pair(Color(0xFF81C784), "🥪")
        "dinner" -> Pair(Color(0xFFE57373), "🍽️")
        else -> Pair(MaterialTheme.colorScheme.primary, "")
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, modifier = Modifier.padding(end = 4.dp))
            Text(
                type,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
private fun NutrientGrid(calories: Int, protein: Int, carbs: Int, fat: Int) {
    val nutrients = listOf(
        NutrientItem("Calories", "$calories kcal", MaterialTheme.colorScheme.primary),
        NutrientItem("Protein", "${protein}g", Color(0xFF00C853)),
        NutrientItem("Carbs", "${carbs}g", Color(0xFFFF9800)),
        NutrientItem("Fat", "${fat}g", Color(0xFFE91E63))
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        nutrients.forEach { item ->
            NutrientPill(item.label, item.value, item.color)
        }
    }
}

@Composable
private fun NutrientPill(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun NutritionBreakdownSection(nutritionSummary: NutritionSummary) {
    ElevatedCard(
        modifier = Modifier.padding(vertical = 16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Nutrition Breakdown",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp))

            val nutrients = listOf(
                NutrientProgressData("Carbs", nutritionSummary.carbs, nutritionSummary.carbsGoal, Color(0xFFFF9800)),
                NutrientProgressData("Protein", nutritionSummary.protein, nutritionSummary.proteinGoal, Color(0xFF00C853)),
                NutrientProgressData("Fat", nutritionSummary.fat, nutritionSummary.fatGoal, Color(0xFFE91E63)),
                NutrientProgressData("Fiber", nutritionSummary.fiber, nutritionSummary.fiberGoal, Color(0xFF9C27B0))
            )

            nutrients.forEach { data ->
                NutrientProgressRow(
                    label = data.label,
                    current = data.current,
                    goal = data.goal,
                    color = data.color
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun NutrientProgressRow(label: String, current: Int, goal: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("$current/$goal", style = MaterialTheme.typography.bodyMedium)
    }
    LinearProgressIndicator(
        progress = current.toFloat() / goal,
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = color,
        trackColor = color.copy(alpha = 0.2f)
    )
}

@Composable
private fun HydrationExerciseRow(waterGlasses: MutableState<Int>, exerciseCalories: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HydrationCard(waterGlasses, modifier = Modifier.weight(1f))
        ExerciseCard(exerciseCalories, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun HydrationCard(waterGlasses: MutableState<Int>, modifier: Modifier = Modifier) {
    ElevatedCard(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.WaterDrop,
                contentDescription = "Water",
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(32.dp))

            Text(
                "${waterGlasses.value}/8",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                ),
                modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "Glasses of Water",
                style = MaterialTheme.typography.bodyMedium)

            WaterControls(waterGlasses)
        }
    }
}

@Composable
private fun WaterControls(waterGlasses: MutableState<Int>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = { if (waterGlasses.value > 0) waterGlasses.value-- },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.LightGray.copy(alpha = 0.2f))
        ) {
            Icon(Icons.Default.Remove, "Decrease")
        }

        WaterLevelIndicator(glasses = waterGlasses.value)

        IconButton(
            onClick = { if (waterGlasses.value < 8) waterGlasses.value++ },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color(0xFF2196F3).copy(alpha = 0.2f))
        ) {
            Icon(Icons.Default.Add, "Increase", tint = Color(0xFF2196F3))
        }
    }
}

@Composable
private fun WaterLevelIndicator(glasses: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(8) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (index < glasses) Color(0xFF2196F3)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
            )
        }
    }
}

@Composable
private fun ExerciseCard(calories: Int, modifier: Modifier = Modifier) {
    ElevatedCard(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.DirectionsRun,
                contentDescription = "Exercise",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp))

            Text(
                "$calories kcal",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                ),
                modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "Calories Burned",
                style = MaterialTheme.typography.bodyMedium)
            Text(
                "Today",
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}
data class NutrientProgressData(
    val label: String,
    val current: Int,
    val goal: Int,
    val color: Color
)
data class NutrientItem(
    val label: String,
    val value: String,
    val color: Color
)
