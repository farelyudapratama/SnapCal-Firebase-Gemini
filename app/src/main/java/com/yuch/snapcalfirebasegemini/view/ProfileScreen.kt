package com.yuch.snapcalfirebasegemini.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.data.api.response.UserPreferences
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel
import java.util.Locale
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    getFoodViewModel: GetFoodViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val email by authViewModel.userEmail.observeAsState("")
    val userPreferences by getFoodViewModel.userPreferences.collectAsState()
    val isLoading by getFoodViewModel.isLoading.collectAsState()

    var activeTab by remember { mutableStateOf("overview") }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login") {
                popUpTo(0)
            }
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        getFoodViewModel.fetchUserPreferences()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF9333EA), // Purple-600
                            Color(0xFF2563EB)  // Blue-600
                        )
                    )
                )
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFFF9FAFB))
            ) {
                // Header with gradient background
                ProfileHeader(
                    email = email,
                    userPreferences = userPreferences,
                    activeTab = activeTab,
                    onTabSelected = { activeTab = it },
                    onSignOut = { authViewModel.signout() }
                )

                // Content based on active tab
                when (activeTab) {
                    "overview" -> OverviewTab(userPreferences = userPreferences)
                    "goals" -> GoalsTab(userPreferences = userPreferences)
                    "preferences" -> PreferencesTab(userPreferences = userPreferences)
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    email: String,
    userPreferences: UserPreferences?,
    activeTab: String,
    onTabSelected: (String) -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF9333EA), // Purple-600
                        Color(0xFF2563EB)  // Blue-600
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top bar with settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            IconButton(
                onClick = onSignOut,
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }

        // User info
        Column {
            Text(
                text = email,
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            userPreferences?.personalInfo?.let { info ->
                Row {
                    Text(
                        text = "${info.age} years",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = " • ",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = info.gender.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.ROOT
                            ) else it.toString()
                        },
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = " • ",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = info.activityLevel.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.ROOT
                            ) else it.toString()
                        },
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Tab navigation
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("overview", "goals", "preferences").forEach { tab ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    TextButton(
                        onClick = { onTabSelected(tab) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (activeTab == tab) Color.White else Color.White.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(
                            text = tab.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (activeTab == tab) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color.White)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewTab(userPreferences: UserPreferences?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        userPreferences?.personalInfo?.let { info ->
            item {
                // Health Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.FitnessCenter,
                        label = "Weight",
                        value = "${info.weight}",
                        unit = "kg",
                        color = Color(0xFF2563EB)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Height,
                        label = "Height",
                        value = "${info.height}",
                        unit = "cm",
                        color = Color(0xFF16A34A)
                    )
                }
            }

            item {
                // BMI Card
                BMICard(height = info.height, weight = info.weight)
            }
        }
    }
}

@Composable
fun GoalsTab(userPreferences: UserPreferences?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Nutrition Goals",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(
                            onClick = { /* TODO Edit goals */ },
                            modifier = Modifier
                                .background(
                                    Color(0xFF9333EA).copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color(0xFF9333EA)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    userPreferences?.dailyGoals?.let { goals ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GoalProgressBar(
                                label = "Calories",
                                current = 1650,
                                target = goals.calories.toInt(),
                                unit = " kcal",
                                color = Color(0xFF3B82F6)
                            )
                            GoalProgressBar(
                                label = "Protein",
                                current = 45,
                                target = goals.protein.toInt(),
                                unit = "g",
                                color = Color(0xFFEF4444)
                            )
                            GoalProgressBar(
                                label = "Carbs",
                                current = 220,
                                target = goals.carbs.toInt(),
                                unit = "g",
                                color = Color(0xFFF59E0B)
                            )
                            GoalProgressBar(
                                label = "Fat",
                                current = 50,
                                target = goals.fat.toInt(),
                                unit = "g",
                                color = Color(0xFF10B981)
                            )
                            GoalProgressBar(
                                label = "Fiber",
                                current = 18,
                                target = goals.fiber.toInt(),
                                unit = "g",
                                color = Color(0xFF8B5CF6)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreferencesTab(userPreferences: UserPreferences?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        userPreferences?.let { prefs ->
            // Health Conditions
            item {
                PreferenceCard(
                    title = "Health Conditions",
                    icon = Icons.Default.LocalHospital,
                    iconColor = Color(0xFFEF4444),
                    items = prefs.healthConditions + prefs.customHealthConditions,
                    tagColor = Color(0xFFEF4444).copy(alpha = 0.1f),
                    tagTextColor = Color(0xFFEF4444)
                )
            }

            // Allergies
            item {
                PreferenceCard(
                    title = "Allergies",
                    icon = Icons.Default.Warning,
                    iconColor = Color(0xFFF59E0B),
                    items = prefs.allergies + prefs.customAllergies,
                    tagColor = Color(0xFFF59E0B).copy(alpha = 0.1f),
                    tagTextColor = Color(0xFFF59E0B)
                )
            }

            // Dietary Restrictions
            item {
                PreferenceCard(
                    title = "Dietary Restrictions",
                    icon = Icons.Default.Restaurant,
                    iconColor = Color(0xFF8B5CF6),
                    items = prefs.dietaryRestrictions,
                    tagColor = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                    tagTextColor = Color(0xFF8B5CF6)
                )
            }

            // Liked Foods
            item {
                PreferenceCard(
                    title = "Liked Foods",
                    icon = Icons.Default.Favorite,
                    iconColor = Color(0xFF10B981),
                    items = prefs.likedFoods,
                    tagColor = Color(0xFF10B981).copy(alpha = 0.1f),
                    tagTextColor = Color(0xFF10B981)
                )
            }

            // Disliked Foods
            item {
                PreferenceCard(
                    title = "Disliked Foods",
                    icon = Icons.Default.ThumbDown,
                    iconColor = Color(0xFF6B7280),
                    items = prefs.dislikedFoods,
                    tagColor = Color(0xFF6B7280).copy(alpha = 0.1f),
                    tagTextColor = Color(0xFF6B7280)
                )
            }

            // Update Button
            item {
                Button(
                    onClick = { /* TODO Update preferences */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9333EA)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Update Preferences",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = value,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = unit,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = color
            )
        }
    }
}

@Composable
fun BMICard(height: Int, weight: Int) {
    val bmi = weight / (height / 100.0).pow(2)
    val bmiCategory = when {
        bmi < 18.5 -> "Underweight" to Color(0xFF3B82F6)
        bmi < 25 -> "Normal" to Color(0xFF10B981)
        bmi < 30 -> "Overweight" to Color(0xFFF59E0B)
        else -> "Obese" to Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Body Mass Index",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFEF4444)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = String.format("%.1f", bmi),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = bmiCategory.first,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = bmiCategory.second
                )
            }
        }
    }
}

@Composable
fun GoalProgressBar(
    label: String,
    current: Int,
    target: Int,
    unit: String,
    color: Color
) {
    val percentage = (current.toFloat() / target.toFloat()).coerceAtMost(1f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$current/$target$unit",
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFFE5E7EB), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun PreferenceCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    items: List<String>,
    tagColor: Color,
    tagTextColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (items.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { item ->
                        Surface(
                            color = tagColor,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = item,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = tagTextColor
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No items added",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        var currentRow = 0
        var currentOrigin = IntOffset.Zero
        val rowSizes = mutableListOf<IntSize>()
        val placeableRowIndex = mutableListOf<Int>()

        placeables.forEach { placeable ->
            if (currentOrigin.x + placeable.width > constraints.maxWidth) {
                currentRow++
                currentOrigin = IntOffset(0, currentOrigin.y + (rowSizes.getOrNull(currentRow - 1)?.height ?: 0) + 8)
            }

            placeableRowIndex.add(currentRow)

            if (rowSizes.size <= currentRow) {
                rowSizes.add(IntSize.Zero)
            }

            rowSizes[currentRow] = IntSize(
                width = maxOf(rowSizes[currentRow].width, currentOrigin.x + placeable.width),
                height = maxOf(rowSizes[currentRow].height, placeable.height)
            )

            currentOrigin = currentOrigin.copy(x = currentOrigin.x + placeable.width + 8)
        }

        val totalHeight = rowSizes.sumOf { it.height } + (rowSizes.size - 1) * 8
        val totalWidth = rowSizes.maxOfOrNull { it.width } ?: 0

        layout(totalWidth, totalHeight) {
            var yPosition = 0
            placeables.forEachIndexed { index, placeable ->
                val rowIndex = placeableRowIndex[index]
                if (index > 0 && placeableRowIndex[index - 1] != rowIndex) {
                    yPosition += rowSizes[rowIndex - 1].height + 8
                }

                val xPosition = if (index == 0 || placeableRowIndex[index - 1] != rowIndex) {
                    0
                } else {
                    placeables.take(index)
                        .filterIndexed { i, _ -> placeableRowIndex[i] == rowIndex }
                        .sumOf { it.width } + (placeables.take(index).count { placeableRowIndex[placeables.indexOf(it)] == rowIndex }) * 8
                }

                placeable.placeRelative(x = xPosition, y = yPosition)
            }
        }
    }
}