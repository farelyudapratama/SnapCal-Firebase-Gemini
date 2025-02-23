@file:OptIn(
    ExperimentalMaterial3Api::class
)

package com.yuch.snapcalfirebasegemini.view

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.api.response.NutritionData
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    foodViewModel: GetFoodViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val email by authViewModel.userEmail.observeAsState("")
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }

    val foodList by foodViewModel.foodList.collectAsStateWithLifecycle()
    val isLoading by foodViewModel.isLoading.collectAsStateWithLifecycle()
    val hasMoreData by foodViewModel.hasMoreData.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    // Handle back button and auth state
    BackHandler { handleBackPress(context) { backPressedTime = it } }
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login")
        }
    }

    Scaffold(
        topBar = { MainTopBar(email = email) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Date and Time Section
            DateTimeHeader()

            // Main Content
            PullToRefreshBox(
                modifier = Modifier.fillMaxWidth(),
                state = refreshState,
                isRefreshing = isRefreshing,
                onRefresh = {
                    coroutineScope.launch {
                        isRefreshing = true
                        foodViewModel.refreshFood()
                        isRefreshing = false
                    }
                }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = foodList,
                        key = { it.id }
                    ) { foodItem ->
                        FoodItemCard(foodItem)
                    }

                    // Loading and Load More
                    item {
                        LoadingAndLoadMore(
                            isLoading = isLoading,
                            hasMoreData = hasMoreData,
                            onLoadMore = { foodViewModel.loadNextPage() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainTopBar(email: String) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "SnapCal",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun DateTimeHeader() {
    val currentDateTime = remember { LocalDateTime.now() }
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateTimeItem(
                icon = Icons.Outlined.DateRange,
                text = currentDateTime.format(dateFormatter)
            )
            Divider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
            )
            DateTimeItem(
                icon = Icons.Outlined.Schedule,
                text = currentDateTime.format(timeFormatter)
            )
        }
    }
}

@Composable
fun DateTimeItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun FoodItemCard(food: FoodItem) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Image and Basic Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FoodImage(food.imageUrl)
                Spacer(modifier = Modifier.width(16.dp))
                FoodBasicInfo(food)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nutrition Info
            NutritionInfoRow(food.nutritionData)

            // Meal Type and Timestamp
            FoodMetadata(food)
        }
    }
}

@Composable
fun FoodImage(imageUrl: String?) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .transformations(CircleCropTransformation())
            .build(),
        contentDescription = null,
        modifier = Modifier
            .size(72.dp)
            .clip(MaterialTheme.shapes.medium),
        loading = {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .padding(4.dp)
            )
        },
        error = {
            Icon(
                imageVector = Icons.Default.BrokenImage,
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
        }
    )
}

@Composable
fun FoodBasicInfo(food: FoodItem) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = food.foodName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "${food.nutritionData.calories} kcal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun NutritionInfoRow(nutritionData: NutritionData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        NutritionItem(Icons.Default.Grain, "${nutritionData.carbs}g", "Carbs")
        NutritionItem(Icons.Default.FitnessCenter, "${nutritionData.protein}g", "Protein")
        NutritionItem(Icons.Default.Water, "${nutritionData.totalFat}g", "Fat")
        NutritionItem(Icons.Default.Grass, "${nutritionData.fiber}g", "Fiber")
        NutritionItem(Icons.Default.Cookie, "${nutritionData.sugar}g", "Sugar")
    }
}

@Composable
fun NutritionItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FoodMetadata(food: FoodItem) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
    val createdAt = LocalDateTime.parse(food.createdAt.substring(0, 19))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Replace Chip with Surface for meal type
        Surface(
            modifier = Modifier.padding(end = 8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = food.mealType.capitalize(
                    Locale.ROOT),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(
            text = createdAt.format(formatter),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LoadingAndLoadMore(
    isLoading: Boolean,
    hasMoreData: Boolean,
    onLoadMore: () -> Unit
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        hasMoreData -> {
            FilledTonalButton(
                onClick = onLoadMore,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Load More")
            }
        }
    }
}

fun handleBackPress(
    context: Context,
    updateBackPressedTime: (Long) -> Unit
) {
    val currentTime = System.currentTimeMillis()
    val backPressedTime = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .getLong("back_pressed_time", 0L)

    if (currentTime - backPressedTime < 2000) {
        android.os.Process.killProcess(android.os.Process.myPid())
    } else {
        updateBackPressedTime(currentTime)
        Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
    }
}