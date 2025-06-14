package com.yuch.snapcalfirebasegemini.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.yuch.snapcalfirebasegemini.data.api.response.UserPreferences
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.FoodViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileViewModel
import java.util.Locale
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val email by authViewModel.userEmail.observeAsState("")
    val userPreferences by profileViewModel.userPreferences.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login") { popUpTo(0) }
        }
    }
    LaunchedEffect(Unit) {
        profileViewModel.fetchUserPreferences()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF5B21B6), Color(0xFF9333EA)),
                    endY = 400f
                )
            )
    ) {
        Scaffold(
            topBar = {
                ProfileTopAppBar(email = email, onSignOut = { authViewModel.signout() })
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                if (userPreferences?.personalInfo == null) {
                    EmptyStateCard(
                        message = "Personal info kamu belum diisi. Yuk lengkapi sekarang!",
                        onActionClick = {
                             navController.navigate("profile_onboarding")
                        },
                        actionLabel = "Isi Sekarang"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(
                                color = Color(0xFFF9FAFB),
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                            ),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        item {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            SectionHeader("Overview")
                        }
                        userPreferences?.personalInfo?.let { info ->
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                BMICard(height = info.height, weight = info.weight)
                            }
                        }
                        item {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            SectionHeader("Daily Goals")
                        }
                        item {
                            GoalsCard(userPreferences = userPreferences, onEditClick = {
                                // navController.navigate("edit_goals_screen")
                            })
                        }
                        item {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            SectionHeader("My Preferences")
                        }
                        item {
                            AllPreferencesCard(userPreferences = userPreferences, onEditClick = {
                                // navController.navigate("edit_preferences_screen")
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    message: String,
    onActionClick: () -> Unit,
    actionLabel: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFF57C00),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    message,
                    color = Color(0xFFF57C00),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onActionClick) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopAppBar(email: String, onSignOut: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text("My Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(email, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        actions = {
            IconButton(onClick = onSignOut) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White)
    )
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
    )
}

@Composable
fun UserProfileCard(email: String, weight: Int?, height: Int?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                tint = Color(0xFF9333EA)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Hi, $email", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "Weight: ${weight ?: "-"} kg | Height: ${height ?: "-"} cm",
                    fontSize = 12.sp, color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun DailySummaryBanner(calories: Int, goal: Int) {
    val progress = if (goal > 0) calories * 100 / goal else 0
    val text = when {
        progress < 50 -> "You're just getting started. Keep going!"
        progress < 100 -> "Almost there! You're doing great."
        else -> "Congrats! You've hit your daily goal 🎉"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE9FE)),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Medium,
            color = Color(0xFF7C3AED),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, icon: ImageVector, label: String, value: String, unit: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, fontSize = 12.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
                    Text(unit, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
                }
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color)
            }
        }
    }
}

@Composable
fun BMICard(height: Int, weight: Int) {
    val bmi = if (height > 0) weight / (height / 100.0).pow(2) else 0.0
    val (category, color) = when {
        bmi < 18.5 -> "Underweight" to Color(0xFF3B82F6)
        bmi < 25 -> "Normal" to Color(0xFF10B981)
        bmi < 30 -> "Overweight" to Color(0xFFF59E0B)
        else -> "Obese" to Color(0xFFEF4444)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Body Mass Index (BMI)", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(String.format(Locale.US, "%.1f", bmi), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = color)
                Spacer(Modifier.width(12.dp))
                Text(category, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = color)
            }
        }
    }
}

@Composable
fun GoalsCard(userPreferences: UserPreferences?, onEditClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Nutrition Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Goals", tint = Color(0xFF7C3AED))
                }
            }
            Spacer(Modifier.height(16.dp))
            userPreferences?.dailyGoals?.let { goals ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GoalProgressBar("Calories", 1650, goals.calories.toInt(), "kcal", Color(0xFF3B82F6))
                    GoalProgressBar("Protein", 45, goals.protein.toInt(), "g", Color(0xFFEF4444))
                    GoalProgressBar("Carbs", 220, goals.carbs.toInt(), "g", Color(0xFFF59E0B))
                    GoalProgressBar("Fat", 50, goals.fat.toInt(), "g", Color(0xFF10B981))
                }
            } ?: Text("No goals set yet.", color = Color.Gray)
        }
    }
}

@Composable
fun AllPreferencesCard(userPreferences: UserPreferences?, onEditClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Preferences & Restrictions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Preferences", tint = Color(0xFF7C3AED))
                }
            }
            Divider(Modifier.padding(vertical = 12.dp))
            userPreferences?.let {
                PreferenceItem(Icons.Default.LocalHospital, "Health Conditions", it.healthConditions + it.customHealthConditions)
                PreferenceItem(Icons.Default.Warning, "Allergies", it.allergies + it.customAllergies)
                PreferenceItem(Icons.Default.RestaurantMenu, "Dietary Types", it.dietaryRestrictions)
                PreferenceItem(Icons.Default.Favorite, "Liked Foods", it.likedFoods)
                PreferenceItem(Icons.Default.ThumbDown, "Disliked Foods", it.dislikedFoods)
            } ?: Text("No preferences set yet.", color = Color.Gray)
        }
    }
}

@Composable
fun PreferenceItem(icon: ImageVector, title: String, items: List<String>) {
    if (items.isNotEmpty()) {
        Column(Modifier.padding(bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text(item.replaceFirstChar { it.titlecase(Locale.ROOT) },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
fun GoalProgressBar(label: String, current: Int, target: Int, unit: String, color: Color) {
    val percentage = if (target > 0) (current.toFloat() / target.toFloat()).coerceAtMost(1f) else 0f
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("${(percentage * 100).toInt()}%", fontSize = 12.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
        Spacer(Modifier.height(4.dp))
        Text("$current / $target $unit", fontSize = 12.sp, color = Color.DarkGray)
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val hSpacing = horizontalArrangement.spacing.roundToPx()
        val vSpacing = verticalArrangement.spacing.roundToPx()
        val rows = mutableListOf<List<Placeable>>()
        val rowHeights = mutableListOf<Int>()
        var currentRow = mutableListOf<Placeable>()
        var currentWidth = 0
        var currentHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)
            if (currentWidth + placeable.width > constraints.maxWidth) {
                rows.add(currentRow); rowHeights.add(currentHeight)
                currentRow = mutableListOf()
                currentWidth = 0; currentHeight = 0
            }
            currentRow.add(placeable)
            currentWidth += placeable.width + hSpacing
            currentHeight = maxOf(currentHeight, placeable.height)
        }
        if (currentRow.isNotEmpty()) { rows.add(currentRow); rowHeights.add(currentHeight) }

        val totalHeight = rowHeights.sum() + maxOf(0, rows.size - 1) * vSpacing
        layout(constraints.maxWidth, totalHeight) {
            var y = 0
            rows.forEachIndexed { i, row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + hSpacing
                }
                y += rowHeights[i] + vSpacing
            }
        }
    }
}
