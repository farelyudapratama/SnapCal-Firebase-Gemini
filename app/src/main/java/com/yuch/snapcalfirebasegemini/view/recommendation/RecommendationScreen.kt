package com.yuch.snapcalfirebasegemini.view.recommendation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yuch.snapcalfirebasegemini.BuildConfig
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.ui.theme.AppColors
import com.yuch.snapcalfirebasegemini.view.recommendation.components.EmptyRecommendationState
import com.yuch.snapcalfirebasegemini.view.recommendation.components.ErrorRecommendationState
import com.yuch.snapcalfirebasegemini.view.recommendation.components.FoodRecommendationCard
import com.yuch.snapcalfirebasegemini.view.recommendation.components.MealTypeSelector
import com.yuch.snapcalfirebasegemini.view.recommendation.components.RecommendationMetadataCard
import com.yuch.snapcalfirebasegemini.viewmodel.RecommendationState
import com.yuch.snapcalfirebasegemini.viewmodel.RecommendationViewModel
import kotlinx.coroutines.delay

private const val TAG = "RecommendationScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    viewModel: RecommendationViewModel,
    navController: NavController? = null,
    onBack: () -> Unit
) {
    var selectedMealType by remember { mutableStateOf("breakfast") }
    var initialLoadDone by remember { mutableStateOf(false) }
    var autoRefreshAttempted by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsStateWithLifecycle()

    if (BuildConfig.DEBUG) Log.d(TAG, "Screen recomposition - Current state: $state")
    when (state) {
        is RecommendationState.Success -> {
            val data = (state as RecommendationState.Success).data
            if (BuildConfig.DEBUG) Log.d(TAG, "SUCCESS STATE - Recommendations count: ${data.recommendations.size}")
        }
        is RecommendationState.Error -> {
            if (BuildConfig.DEBUG) Log.d(TAG, "ERROR STATE - Message: ${(state as RecommendationState.Error).message}")
        }
        is RecommendationState.Loading -> if (BuildConfig.DEBUG) Log.d(TAG, "LOADING STATE")
        is RecommendationState.Initial -> if (BuildConfig.DEBUG) Log.d(TAG, "INITIAL STATE")
    }

    LaunchedEffect(state) {
        if (state is RecommendationState.Initial && initialLoadDone && !autoRefreshAttempted) {
            delay(1000)
            if (state is RecommendationState.Initial) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Auto-triggering refresh because we're stuck in Initial state")
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
                    containerColor = AppColors.BrandAmber,
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
                        colors = listOf(AppColors.GradientStart, AppColors.GradientEnd),
                        endY = 200f
                    )
                )
                .padding(paddingValues)
        ) {
            LaunchedEffect(Unit) {
                if (!initialLoadDone) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Initial load triggered with refresh=true")
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
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (BuildConfig.DEBUG) Log.d(TAG, "Manual refresh triggered")
                            viewModel.loadRecommendations(selectedMealType, refresh = true)
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }

                    }
                }

                item {
                    MealTypeSelector(
                        selectedMealType = selectedMealType,
                        onMealTypeSelected = { newType ->
                            selectedMealType = newType
                            if (BuildConfig.DEBUG) Log.d(TAG, "Meal type changed to: $newType")
                            viewModel.loadRecommendations(newType, refresh = false)
                        }
                    )
                }

                when (state) {
                    is RecommendationState.Loading -> {
                        item {
                            if (BuildConfig.DEBUG) Log.d(TAG, "Rendering loading state")
                            RecommendationLoadingContent(
                                message = stringResource(R.string.generating_recommendations)
                            )
                        }
                    }
                    is RecommendationState.Success -> {
                        val data = (state as RecommendationState.Success).data
                        if (BuildConfig.DEBUG) Log.d(TAG, "Rendering success state with ${data.recommendations.size} recommendations")

                        if (data.recommendations.isNotEmpty()) {
                            item { RecommendationMetadataCard(data.metadata) }

                            items(data.recommendations) { recommendation ->
                                FoodRecommendationCard(recommendation)
                            }
                        } else {
                            item { EmptyRecommendationState() }
                        }
                    }
                    is RecommendationState.Error -> {
                        item {
                            val message = (state as RecommendationState.Error).message
                            if (BuildConfig.DEBUG) Log.d(TAG, "Rendering error state: $message")
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
                            if (BuildConfig.DEBUG) Log.d(TAG, "Rendering initial state")
                            RecommendationLoadingContent(
                                message = "Mempersiapkan rekomendasi makanan...",
                                showRetry = initialLoadDone,
                                onRetry = {
                                    viewModel.loadRecommendations(selectedMealType, refresh = true)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationLoadingContent(
    message: String,
    showRetry: Boolean = false,
    onRetry: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AppColors.BrandAmber)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (showRetry) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.BrandAmber
                    )
                ) {
                    Text("Coba Lagi", color = Color.White)
                }
            }
        }
    }
}
