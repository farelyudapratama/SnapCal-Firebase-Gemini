package com.yuch.snapcalfirebasegemini.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.data.api.response.FoodRecommendation
import com.yuch.snapcalfirebasegemini.viewmodel.RecommendationState
import com.yuch.snapcalfirebasegemini.viewmodel.RecommendationViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

// Create a tag for better logging
private const val TAG = "RecommendationScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    viewModel: RecommendationViewModel = viewModel(),
    navController: NavController? = null,
    onBack: () -> Unit
) {
    var selectedMealType by remember { mutableStateOf("breakfast") }
    var initialLoadDone by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()

    Log.d(TAG, "Screen recomposition - Current state: $state")
    when (state) {
        is RecommendationState.Success -> {
            val data = (state as RecommendationState.Success).data
            Log.d(TAG, "SUCCESS STATE - Recommendations count: ${data.recommendations.size}")
        }
        is RecommendationState.Error -> {
            Log.d(TAG, "ERROR STATE - Message: ${(state as RecommendationState.Error).message}")
        }
        is RecommendationState.Loading -> {
            Log.d(TAG, "LOADING STATE")
        }
        is RecommendationState.Initial -> {
            Log.d(TAG, "INITIAL STATE")
        }
    }

    var autoRefreshAttempted by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is RecommendationState.Initial && initialLoadDone && !autoRefreshAttempted) {
            delay(1000)
            if (state is RecommendationState.Initial) {
                Log.d(TAG, "Auto-triggering refresh because we're stuck in Initial state")
                viewModel.loadRecommendations(selectedMealType, refresh = true)
                autoRefreshAttempted = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.ai_food_assistant),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFB67321),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFB67321), Color(0xFFEA4233)),
                        endY = 200f
                    )
                )
                .padding(paddingValues)
        ) {
            // Load data on first launch
            LaunchedEffect(Unit) {
                if (!initialLoadDone) {
                    Log.d(TAG, "Initial load triggered with refresh=true")
                    viewModel.loadRecommendations(selectedMealType, refresh = true)
                    initialLoadDone = true
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color(0xFFF9FAFB),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    ),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Refresh button at top
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            Log.d(TAG, "Manual refresh triggered")
                            viewModel.loadRecommendations(selectedMealType, refresh = true)
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }

                        // Debug info to show current state - remove in production
                        Text(
                            text = when(state) {
                                is RecommendationState.Initial -> "Initial"
                                is RecommendationState.Loading -> "Loading"
                                is RecommendationState.Success -> "Success"
                                is RecommendationState.Error -> "Error"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                // Meal type selector
                item {
                    MealTypeSelector(
                        selectedMealType = selectedMealType,
                        onMealTypeSelected = { newType ->
                            selectedMealType = newType
                            Log.d(TAG, "Meal type changed to: $newType")
                            viewModel.loadRecommendations(newType, refresh = false)
                        }
                    )
                }

                // Content based on state
                when (state) {
                    is RecommendationState.Loading -> {
                        item {
                            Log.d(TAG, "Rendering loading state")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = Color(0xFFB67321))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        stringResource(R.string.generating_recommendations),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    is RecommendationState.Success -> {
                        val data = (state as RecommendationState.Success).data
                        Log.d(TAG, "Rendering success state with ${data.recommendations.size} recommendations")

                        if (data.recommendations.isNotEmpty()) {
                            // Metadata card
                            item {
                                RecommendationMetadataCard(data.metadata)
                            }

                            // Food recommendations
                            items(data.recommendations) { recommendation ->
                                FoodRecommendationCard(recommendation)
                            }
                        } else {
                            item {
                                EmptyRecommendationState()
                            }
                        }
                    }

                    is RecommendationState.Error -> {
                        item {
                            val message = (state as RecommendationState.Error).message
                            Log.d(TAG, "Rendering error state: $message")
                            ErrorRecommendationState(
                                message = message,
                                onRetry = {
                                    viewModel.loadRecommendations(selectedMealType, refresh = true)
                                },
                                navController = navController
                            )
                        }
                    }

                    is RecommendationState.Initial -> {
                        item {
                            Log.d(TAG, "Rendering initial state")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = Color(0xFFB67321))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Mempersiapkan rekomendasi makanan...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (initialLoadDone) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                viewModel.loadRecommendations(selectedMealType, refresh = true)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFB67321)
                                            )
                                        ) {
                                            Text("Coba Lagi", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MealTypeSelector(
    selectedMealType: String,
    onMealTypeSelected: (String) -> Unit
) {
    val mealTypes = listOf(
        Triple("breakfast", "Breakfast", Icons.Default.FreeBreakfast),
        Triple("lunch", "Lunch", Icons.Default.LunchDining),
        Triple("dinner", "Dinner", Icons.Default.DinnerDining)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.select_meal_type),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                mealTypes.forEach { (type, label, icon) ->
                    MealTypeChip(
                        type = type,
                        label = label,
                        icon = icon,
                        isSelected = selectedMealType == type,
                        onSelected = { onMealTypeSelected(type) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MealTypeChip(
    type: String,
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clickable { onSelected() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFB67321) else Color.Transparent
        ),
        border = if (!isSelected) CardDefaults.outlinedCardBorder() else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else Color(0xFFB67321),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) Color.White else Color(0xFFB67321),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RecommendationMetadataCard(metadata: com.yuch.snapcalfirebasegemini.data.api.response.RecommendationMetadata) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (metadata.fallbackUsed) Icons.Default.Warning else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (metadata.fallbackUsed) Color(0xFFF59E0B) else Color(0xFF0369A1),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = metadata.modelUsed,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (metadata.fallbackUsed) {
                                Text(
                                    text = stringResource(R.string.fallback_mode),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                            if (metadata.fromCache) {
                                Text(
                                    text = stringResource(R.string.from_cache),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF059669)
                                )
                            }
                        }
                    }
                }

                // Format date
                val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
                val dateTime = try {
                    Instant.parse(metadata.generatedAt)
                        .atZone(ZoneId.systemDefault())
                        .format(formatter)
                } catch (e: Exception) {
                    "Recently"
                }

                Text(
                    text = dateTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF0369A1)
                )
            }
        }
    }
}

@Composable
fun FoodRecommendationCard(recommendation: FoodRecommendation) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recommendation.foodName.takeIf { it.isNotBlank() } ?: "Unknown Food",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recommendation.description.takeIf { it.isNotBlank() } ?: "No description available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = Color(0xFFB67321).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${recommendation.calories} kcal",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFB67321),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Macros grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroItem("Carbs", "${recommendation.macros.carbs}g", Icons.Default.Grain)
                MacroItem("Protein", "${recommendation.macros.protein}g", Icons.Default.FitnessCenter)
                MacroItem("Fat", "${recommendation.macros.fat}g", Icons.Default.Water)
                MacroItem("Fiber", "${recommendation.macros.fiber}g", Icons.Default.Grass)
            }

            if (recommendation.reasoning.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.why_this_recommendation),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = recommendation.reasoning.takeIf { it.isNotBlank() } ?: "No reasoning provided",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MacroItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyRecommendationState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.no_recommendations_available),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.try_refreshing_different_meal),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorRecommendationState(
    message: String,
    onRetry: () -> Unit,
    navController: NavController? = null
) {
    // Check if error is related to missing profile data
    val isProfileDataMissing = message.contains("400", ignoreCase = true) ||
            message.contains("profile", ignoreCase = true) ||
            message.contains("personal info", ignoreCase = true) ||
            message.contains("preferences", ignoreCase = true) ||
            message.contains("data tidak lengkap", ignoreCase = true) ||
            message.contains("silakan lengkapi", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isProfileDataMissing) Color(0xFFFEF3C7) else Color(0xFFFEF2F2)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isProfileDataMissing) Icons.Default.Person else Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (isProfileDataMissing) Color(0xFFD97706) else Color(0xFFDC2626)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isProfileDataMissing)
                    stringResource(R.string.profile_incomplete_title)
                else
                    stringResource(R.string.something_went_wrong),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isProfileDataMissing) Color(0xFFD97706) else Color(0xFFDC2626),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isProfileDataMissing)
                    stringResource(R.string.profile_incomplete_message)
                else
                    message,
                style = MaterialTheme.typography.bodySmall,
                color = if (isProfileDataMissing) Color(0xFFA16207) else Color(0xFF991B1B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isProfileDataMissing && navController != null) {
                Button(
                    onClick = {
                        navController.navigate("profile_onboarding?edit=false")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD97706)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.complete_profile),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onRetry) {
                    Text(
                        stringResource(R.string.try_again),
                        color = Color(0xFFD97706)
                    )
                }
            } else {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626)
                    )
                ) {
                    Text(stringResource(R.string.try_again), color = Color.White)
                }
            }
        }
    }
}
