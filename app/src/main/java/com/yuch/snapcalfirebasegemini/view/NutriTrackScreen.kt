package com.yuch.snapcalfirebasegemini.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Data models
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutriTrackScreen(navController: NavController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login"){
                popUpTo(0)
            }
            else -> Unit
        }
    }
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
    // Main layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "NutriTrack",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // Main layout
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp)  // Added extra padding for bottom bar clearance
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Current date display
                Text(
                    text = "Diet & Nutrition Tracker - ${LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Summary cards row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Today's Summary Card
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        ),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Today's Summary",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Calories progress
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Calories",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${nutritionSummary.calories} / ${nutritionSummary.caloriesGoal}",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = nutritionSummary.calories.toFloat() / nutritionSummary.caloriesGoal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Protein progress
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Protein",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${nutritionSummary.protein}g / ${nutritionSummary.proteinGoal}g",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = nutritionSummary.protein.toFloat() / nutritionSummary.proteinGoal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF00C853),
                                trackColor = Color(0xFF00C853).copy(alpha = 0.2f)
                            )
                        }
                    }

                    // Weekly Progress Card
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        ),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Weekly Progress",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Avg. Calories:",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${weeklyProgress.avgCalories}",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val weightChangeText = if (weeklyProgress.weightChange < 0) {
                                "Weight Change: ${weeklyProgress.weightChange}kg"
                            } else {
                                "Weight Change: +${weeklyProgress.weightChange}kg"
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = weightChangeText,
                                    color = if (weeklyProgress.weightChange < 0)
                                        Color(0xFF00C853) else Color.Red
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Workouts: ${weeklyProgress.workoutCount}",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Meal tracking section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Meal Tracking - Today",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    FilledTonalButton(
                        onClick = { /* Add meal functionality */ },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Meal",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Meal")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Meal entries
            items(mealEntries.size) { index ->
                val meal = mealEntries[index]

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation =
                            1.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = meal.type,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = meal.time,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { /* Delete meal functionality */ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = meal.description,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Detailed nutrition info for each meal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            NutrientBadge("Calories", "${meal.calories} kcal", MaterialTheme.colorScheme.primary)
                            NutrientBadge("Protein", "${meal.protein}g", Color(0xFF00C853))
                            NutrientBadge("Carbs", "${meal.carbs}g", Color(0xFFFF9800))
                            NutrientBadge("Fat", "${meal.fat}g", Color(0xFFE91E63))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                // Nutrition breakdown section
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Nutrition Breakdown",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // More detailed nutrition breakdown
                        NutrientProgressRow(
                            label = "Carbs",
                            current = nutritionSummary.carbs,
                            goal = nutritionSummary.carbsGoal,
                            color = Color(0xFFFF9800)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        NutrientProgressRow(
                            label = "Protein",
                            current = nutritionSummary.protein,
                            goal = nutritionSummary.proteinGoal,
                            color = Color(0xFF00C853)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        NutrientProgressRow(
                            label = "Fat",
                            current = nutritionSummary.fat,
                            goal = nutritionSummary.fatGoal,
                            color = Color(0xFFE91E63)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        NutrientProgressRow(
                            label = "Fiber",
                            current = nutritionSummary.fiber,
                            goal = nutritionSummary.fiberGoal,
                            color = Color(0xFF9C27B0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom section: Water tracking and Exercise
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Water tracking
                    ElevatedCard(
                        modifier = Modifier.weight(1f),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WaterDrop,
                                    contentDescription = "Water",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Water Tracking",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "${waterGlasses.value}/8 glasses",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FilledIconButton(
                                    onClick = {
                                        if (waterGlasses.value > 0) waterGlasses.value--
                                    },
                                    modifier = Modifier.size(40.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = Color.LightGray
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Remove glass",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Water glasses visualization
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (i in 1..8) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(24.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (i <= waterGlasses.value) Color(0xFF2196F3)
                                                    else Color.LightGray.copy(alpha = 0.3f)
                                                )
                                        )
                                    }
                                }

                                FilledIconButton(
                                    onClick = {
                                        if (waterGlasses.value < 8) waterGlasses.value++
                                    },
                                    modifier = Modifier.size(40.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = Color(0xFF2196F3)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add glass",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Exercise section
                    ElevatedCard(
                        modifier = Modifier.weight(1f),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DirectionsRun,
                                    contentDescription = "Exercise",
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Exercise",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "$exerciseCaloriesBurned",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )

                            Text(
                                text = "calories burned",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Running",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "30 minutes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Extra padding at the bottom for bottom bar clearance
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun NutrientBadge(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$value",
                color = color,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NutrientProgressRow(label: String, current: Int, goal: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "$current / $goal g",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

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