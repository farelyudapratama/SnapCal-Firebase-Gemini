package com.yuch.snapcalfirebasegemini.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.data.api.response.*
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.FoodViewModel
import kotlinx.coroutines.launch
import kotlin.math.min

// --- Definisi Warna Baru ---
val DarkGreenPrimary = Color(0xFF00695C)
val MediumGreenSecondary = Color(0xFF4DB6AC)
val LightGreenBackground = Color(0xFFE0F2F1) // Bisa juga Color.White jika ingin lebih clean

val AccentYellow = Color(0xFFFFD54F) // Untuk Karbohidrat
val AccentOrange = Color(0xFFFFB74D) // Untuk Lemak / Kalori (progress utama)
val AccentDeepOrange = Color(0xFFFF8A65) // Alternatif untuk kalori/warning
val AccentGreen = Color(0xFF4CAF50) // Untuk Protein (tetap) / Progress Baik
val AccentRed = Color(0xFFF44336) // Untuk warning (tetap) / Progress Buruk
val AccentAmber = Color(0xFFFFC107) // Untuk warning sedang (tetap) / Progress Sedang

val TextOnDark = Color.White
val TextOnLight = Color.DarkGray // Lebih soft dari Black
val TextSecondaryOnLight = Color.Gray
// --- Akhir Definisi Warna Baru ---


@Composable
fun NutriTrackScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    viewModel: FoodViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login") { popUpTo(0) }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchDailySummary()
        viewModel.fetchWeeklySummary()
    }

    val dailySummary by viewModel.dailySummary.collectAsState()
    val weeklySummary by viewModel.weeklySummary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(

                            DarkGreenPrimary,
                            MediumGreenSecondary,
                            LightGreenBackground
                        )
                    )
                )
        )

        if (isLoading && dailySummary == null) {
            LoadingScreen()
        } else if (dailySummary != null) {
            NutriContent(
                summary = dailySummary!!,
                weeklySummary = weeklySummary
            )
        } else {
            EmptyDataScreen {
                viewModel.fetchDailySummary()
                viewModel.fetchWeeklySummary()
            }
        }
    }
}

@Composable
fun NutriContent(
    summary: DailySummaryResponse,
    weeklySummary: WeeklySummaryResponse?
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // Custom Header with Gradient
        CustomHeader(summary.date)

        // Tab Navigation
        TabNavigation(
            selectedTab = pagerState.currentPage,
            onTabSelected = { index ->
                scope.launch {
                    pagerState.animateScrollToPage(index)
                }
            }
        )

        // ViewPager Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> DailyOverviewPage(summary)
                1 -> WeeklyAnalysisPage(weeklySummary)
            }
        }
    }
}

@Composable
fun CustomHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                Brush.horizontalGradient(

                    colors = listOf(DarkGreenPrimary, MediumGreenSecondary)
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Text(
                text = "NutriTrack",
                color = TextOnDark,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = date,
                color = TextOnDark.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
        }

        Icon(
            imageVector = Icons.Default.Restaurant,
            contentDescription = null,
            tint = TextOnDark.copy(alpha = 0.3f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(60.dp)
        )
    }
}

@Composable
fun TabNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Today", "Weekly")
    val icons = listOf(Icons.Default.Dashboard, Icons.Default.Timeline)

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightGreenBackground)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(tabs.size) { index ->
            TabItem(
                title = tabs[index],
                icon = icons[index],
                isSelected = selectedTab == index,
                onClick = { onTabSelected(index) }
            )
        }
    }
}

@Composable
fun TabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(

        targetValue = if (isSelected) DarkGreenPrimary else TextSecondaryOnLight,
        animationSpec = tween(300), label = ""
    )
    val animatedTextColor by animateColorAsState(

        targetValue = if (isSelected) DarkGreenPrimary else TextSecondaryOnLight,
        animationSpec = tween(300), label = "tab_text_color"
    )


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = animatedColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = animatedTextColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(2.dp)
                    .background(

                        color = DarkGreenPrimary,
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

@Composable
fun DailyOverviewPage(summary: DailySummaryResponse) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            CalorieProgressCard(summary)
        }

        item {
            MacronutrientOverview(summary.data)
        }

        item {
            Text(
                "Today's Meals",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextOnLight,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(summary.foods) { food ->
            FoodCard(food)
        }

        if (summary.feedback.isNotEmpty()) {
            item {
                Text(
                    "AI Insights",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextOnLight,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(summary.feedback) { feedback ->
                InsightCard(feedback)
            }
        }

        item {
            DetailedNutritionCard(summary.data, summary.goals)
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun CalorieProgressCard(summary: DailySummaryResponse) {
    val currentCalories = summary.data?.totalCalories ?: 0.0
    val targetCalories = summary.goals.calories
    val progress = if (targetCalories > 0) (currentCalories / targetCalories).toFloat() else 0f


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(

                            MediumGreenSecondary.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    "Daily Calories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextOnLight
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { min(progress, 1f) },
                        modifier = Modifier.size(100.dp),

                        color = when {
                            progress < 0.5f -> AccentGreen
                            progress < 0.9f -> AccentAmber
                            else -> AccentRed
                        },
                        strokeWidth = 8.dp,
                        trackColor = Color.Gray.copy(alpha = 0.2f)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${currentCalories.toInt()}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextOnLight
                        )
                        Text(
                            "/ ${targetCalories.toInt()} kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryOnLight
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MacronutrientOverview(data: DailySummary?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Macronutrients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextOnLight
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (data != null) {

                MacroItem("Carbs", data.totalCarbs, AccentYellow, Icons.Default.Grain)
                MacroItem("Protein", data.totalProtein, AccentGreen, Icons.Default.FitnessCenter)
                MacroItem("Fat", data.totalFat, AccentOrange, Icons.Default.Opacity)
            }
        }
    }
}

@Composable
fun MacroItem(name: String, value: Double, color: Color, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextOnLight
            )
            Text(
                "${value.toInt()}g",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color // Warna spesifik makro
            )
        }
    }
}

@Composable
fun FoodCard(food: FoodBrief) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val mealColor = getMealTypeColor(food.mealType)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        mealColor.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getMealTypeIcon(food.mealType),
                    contentDescription = food.mealType,
                    tint = mealColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    food.mealType,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextOnLight
                )
                Text(
                    food.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryOnLight
                )
            }

            Text(
                "${food.calories.toInt()} kcal",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = mealColor
            )
        }
    }
}

@Composable
fun InsightCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(

            containerColor = MediumGreenSecondary.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = "Insight",

                tint = DarkGreenPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextOnLight,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DetailedNutritionCard(data: DailySummary?, goals: Goals) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Detailed Nutrition",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextOnLight
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (data != null) {

                NutrientProgressBar("Calories", data.totalCalories, goals.calories, "kcal", AccentDeepOrange)
                NutrientProgressBar("Carbohydrates", data.totalCarbs, goals.carbs, "g", AccentYellow)
                NutrientProgressBar("Protein", data.totalProtein, goals.protein, "g", AccentGreen)
                NutrientProgressBar("Fat", data.totalFat, goals.fat, "g", AccentOrange)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Additional Nutrients",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextOnLight
                )
                Spacer(modifier = Modifier.height(12.dp))

                SimpleNutrientItem("Saturated Fat", data.totalSaturatedFat, "g")
                SimpleNutrientItem("Fiber", data.totalFiber, "g")
                SimpleNutrientItem("Sugar", data.totalSugar, "g")
            }
        }
    }
}

@Composable
fun NutrientProgressBar(
    name: String,
    current: Double,
    goal: Double,
    unit: String,
    color: Color
) {
    val progress = if (goal > 0) (current / goal).toFloat() else 0f


    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextOnLight
            )
            Text(
                "${current.toInt()} / ${goal.toInt()} $unit",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryOnLight
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { min(progress, 1f) },
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
fun SimpleNutrientItem(name: String, value: Double, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            name,
            style = MaterialTheme.typography.bodyMedium,
            color = TextOnLight
        )
        Text(
            "${value.toInt()} $unit",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextOnLight
        )
    }
}

@Composable
fun WeeklyAnalysisPage(weeklySummary: WeeklySummaryResponse?) {
    val pageBackgroundColor = Color.White
    if (weeklySummary == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = TextSecondaryOnLight
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Weekly data not available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondaryOnLight
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBackgroundColor)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            WeeklyOverviewCard(weeklySummary)
        }

        item {
            Text(
                "Daily Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextOnLight
            )
        }

        items(weeklySummary.summaries) { day ->
            DailyBreakdownCard(day, weeklySummary.dailyGoal)
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun WeeklyOverviewCard(summary: WeeklySummaryResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Weekly Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextOnLight
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${summary.weekStart} - ${summary.weekEnd}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryOnLight
            )
            Spacer(modifier = Modifier.height(20.dp))

            val avgCalories = summary.summaries.map { it.calories }.average().takeIf { !it.isNaN() } ?: 0.0
            val avgCarbs = summary.summaries.map { it.carbs }.average().takeIf { !it.isNaN() } ?: 0.0
            val avgProtein = summary.summaries.map { it.protein }.average().takeIf { !it.isNaN()} ?: 0.0



            WeeklyStatItem("Average Calories", avgCalories, "kcal", AccentDeepOrange)
            WeeklyStatItem("Average Carbs", avgCarbs, "g", AccentYellow)
            WeeklyStatItem("Average Protein", avgProtein, "g", AccentGreen)
        }
    }
}

@Composable
fun WeeklyStatItem(label: String, value: Double, unit: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            color = TextOnLight
        )
        Text(
            "${value.toInt()} $unit",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun DailyBreakdownCard(day: DailyNutritionSummary, goal: NutrientGoal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                day.date,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextOnLight
            )
            Spacer(modifier = Modifier.height(12.dp))

            val calorieProgress = if(goal.calories > 0) (day.calories / goal.calories).toFloat() else 0f


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                CompactNutrientInfo("Cal", day.calories.toInt(), AccentDeepOrange)
                CompactNutrientInfo("Carbs", day.carbs.toInt(), AccentYellow)
                CompactNutrientInfo("Protein", day.protein.toInt(), AccentGreen)
                CompactNutrientInfo("Fat", day.fat.toInt(), AccentOrange)
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { min(calorieProgress, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),

                color = AccentDeepOrange,
                trackColor = Color.Gray.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun CompactNutrientInfo(label: String, value: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "$value",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryOnLight
        )
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = TextOnDark,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Loading nutrition data...",
                color = TextOnDark,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun EmptyDataScreen(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.NoFood,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextOnDark.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No nutrition data available",
                color = TextOnDark,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Add some food entries to see your nutrition tracking",
                color = TextOnDark.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(

                    containerColor = Color.White,
                    contentColor = DarkGreenPrimary
                )
            ) {
                Text("Retry")
            }
        }
    }
}

// Helper functions
fun getMealTypeColor(mealType: String): Color {
    return when (mealType.lowercase()) {
        "breakfast", "sarapan" -> AccentYellow
        "lunch", "makan siang" -> AccentGreen
        "dinner", "makan malam" -> MediumGreenSecondary
        "snack", "cemilan" -> AccentOrange
        else -> TextSecondaryOnLight
    }
}

fun getMealTypeIcon(mealType: String): ImageVector {
    return when (mealType.lowercase()) {
        "breakfast", "sarapan" -> Icons.Default.WbSunny
        "lunch", "makan siang" -> Icons.Default.LunchDining
        "dinner", "makan malam" -> Icons.Default.DinnerDining
        "snack", "cemilan" -> Icons.Default.Cookie
        else -> Icons.Default.Restaurant
    }
}