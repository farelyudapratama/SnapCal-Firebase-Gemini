package com.yuch.snapcalfirebasegemini.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.data.api.response.FoodRecommendation
import com.yuch.snapcalfirebasegemini.data.api.response.RecommendationData
import com.yuch.snapcalfirebasegemini.ui.navigation.Screen
import com.yuch.snapcalfirebasegemini.viewmodel.RecommendationViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    viewModel: RecommendationViewModel = viewModel(),
    navController: NavController? = null,
    onBack: () -> Unit
) {
    var selectedMealType by remember { mutableStateOf("breakfast") }
    var showInitialChoice by remember { mutableStateOf(true) }

    val isLoading by viewModel.isLoading.collectAsState()
    val result by viewModel.recommendationResult.collectAsState()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "AI Food Assistant",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
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
            if (showInitialChoice) {
                InitialChoiceScreen(
                    onRecommendationChoice = {
                        showInitialChoice = false
                    },
                    onChatChoice = {
                        navController?.navigate(Screen.AiChat.route)
                    }
                )
            } else {
                RecommendationContent(
                    selectedMealType = selectedMealType,
                    onMealTypeChanged = { newType ->
                        selectedMealType = newType
                        viewModel.loadRecommendations(newType, refresh = false)
                    },
                    onRefresh = {
                        viewModel.loadRecommendations(selectedMealType, refresh = true)
                    },
                    isLoading = isLoading,
                    result = result,
                    onBackToChoice = { showInitialChoice = true }
                )
            }
        }
    }
}

@Composable
fun InitialChoiceScreen(
    onRecommendationChoice: () -> Unit,
    onChatChoice: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFB67321)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "What can I help you with?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Choose how you'd like to get nutrition guidance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Recommendation Option
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRecommendationChoice() },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Color(0xFFB67321).copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = Color(0xFFB67321),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Get Meal Recommendations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Smart suggestions for breakfast, lunch, or dinner",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chat Option
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChatChoice() },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Color(0xFFEA4233).copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = Color(0xFFEA4233),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Chat with AI Nutritionist",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ask questions about nutrition and health",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationContent(
    selectedMealType: String,
    onMealTypeChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean,
    result: com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse<RecommendationData>?,
    onBackToChoice: () -> Unit
) {
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
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBackToChoice
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back to Options")
                }

                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, "Refresh")
                }
            }
        }

        item {
            MealTypeSelector(
                selectedMealType = selectedMealType,
                onMealTypeSelected = onMealTypeChanged
            )
        }

        when {
            isLoading -> {
                item {
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
                                "Generating personalized recommendations...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            result?.status == "success" && result.data != null -> {
                val recommendations = result.data.recommendations
                if (recommendations.isNotEmpty()) {
                    item {
                        RecommendationMetadataCard(result.data.metadata)
                    }

                    items(recommendations) { recommendation ->
                        FoodRecommendationCard(recommendation)
                    }
                } else {
                    item {
                        EmptyRecommendationState()
                    }
                }
            }

            else -> {
                item {
                    ErrorRecommendationState(
                        message = result?.message ?: "Failed to load recommendations",
                        onRetry = onRefresh
                    )
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
                text = "Select Meal Type",
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
                                    text = "Fallback mode",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                            if (metadata.fromCache) {
                                Text(
                                    text = "From cache",
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
                                text = "Why this recommendation?",
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
                text = "No recommendations available",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Try refreshing or selecting a different meal type",
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
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFFDC2626)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF991B1B),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626)
                )
            ) {
                Text("Try Again", color = Color.White)
            }
        }
    }
}
