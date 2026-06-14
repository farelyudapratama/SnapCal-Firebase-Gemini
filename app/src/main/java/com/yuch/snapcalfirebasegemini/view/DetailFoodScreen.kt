package com.yuch.snapcalfirebasegemini.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.ui.navigation.Screen
import com.yuch.snapcalfirebasegemini.ui.theme.caloriesColor
import com.yuch.snapcalfirebasegemini.view.fooddetail.components.DateInfoCard
import com.yuch.snapcalfirebasegemini.view.fooddetail.components.DeleteFoodDialog
import com.yuch.snapcalfirebasegemini.view.fooddetail.components.FatBreakdownCard
import com.yuch.snapcalfirebasegemini.view.fooddetail.components.FoodDetailEmptyState
import com.yuch.snapcalfirebasegemini.view.fooddetail.components.FoodHeaderSection
import com.yuch.snapcalfirebasegemini.view.fooddetail.components.NutritionInfoCard
import com.yuch.snapcalfirebasegemini.view.fooddetail.components.NutritionSourceCard
import com.yuch.snapcalfirebasegemini.view.fooddetail.components.showDeleteFoodToast
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.FoodDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailFoodScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    foodId: String,
    onBack: () -> Boolean,
    viewModel: FoodDetailViewModel,
) {
    val authState = authViewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val food by viewModel.food.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isDeleted by viewModel.isDeleted.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate(Screen.Login.route) { popUpTo(0) }
        }
    }

    LaunchedEffect(foodId) {
        viewModel.fetchFoodById(foodId, true)
    }

    LaunchedEffect(navController.currentBackStackEntry) {
        val updated = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("food_updated") ?: false
        if (updated) {
            viewModel.fetchFoodById(foodId, forceRefresh = true)
            navController.currentBackStackEntry?.savedStateHandle?.set("food_updated", false)
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(isDeleted) {
        if (isDeleted) {
            navController.popBackStack()
            viewModel.clearIsDeleted()
        }
    }

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
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.detail_food_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onBack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDialog = true }) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.height(28.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Food",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.EditFood.createRoute(foodId)) },
                    containerColor = caloriesColor
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Food",
                        tint = Color.White
                    )
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
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                food?.let { foodItem ->
                    LazyColumn(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(
                                top = 56.dp,
                                bottom = paddingValues.calculateBottomPadding(),
                                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current)
                            )
                            .background(color = Color(0xFFF9FAFB)),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { FoodHeaderSection(foodItem, context) }
                        item { NutritionSourceCard(foodItem.nutritionData) }
                        item { NutritionInfoCard(foodItem) }
                        item { FatBreakdownCard(foodItem.nutritionData) }
                        item { DateInfoCard(foodItem.createdAt ?: "N/A") }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                } ?: FoodDetailEmptyState(
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }

    if (showDialog) {
        DeleteFoodDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                showDialog = false
                viewModel.deleteFood(foodId) {
                    showDeleteFoodToast(context)
                }
            }
        )
    }
}
