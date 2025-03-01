@file:OptIn(
    ExperimentalMaterial3Api::class
)

package com.yuch.snapcalfirebasegemini.view

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.api.response.NutritionData
import com.yuch.snapcalfirebasegemini.ui.theme.*
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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

    val foodList by foodViewModel.foodList.collectAsState()
    val isLoading by foodViewModel.isLoading.collectAsState()
    val hasMoreData by foodViewModel.hasMoreData.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    // Handle back button and auth state
    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < 2000) {
            (context as? android.app.Activity)?.finishAffinity()
        } else {
            backPressedTime = currentTime
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Authenticated) {
            foodViewModel.refreshFood()
        }
    }

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
                        FoodItemCard(foodItem, navController)
                    }

                    // Loading and Load More
                    item {
                        LoadingAndLoadMore(
                            isLoading = isLoading,
                            hasMoreData = hasMoreData,
                            onLoadMore = { foodViewModel.loadNextPage() }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
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
fun FoodItemCard(
    food: FoodItem,
    navController: NavController
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                navController.navigate("detail-food/${food.id}")
            },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FoodImage(
                    imageUrl = food.imageUrl,
                    nutritionData = food.nutritionData
                )
                Spacer(modifier = Modifier.width(16.dp))
                FoodBasicInfo(food)
            }

            Spacer(modifier = Modifier.height(16.dp))
            NutritionInfoRow(food.nutritionData)
            FoodMetadata(food)
        }
    }
}

@Composable
fun FoodImage(
    imageUrl: String?,
    nutritionData: NutritionData,
    modifier: Modifier = Modifier
) {
    if (imageUrl != null) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .transformations(CircleCropTransformation())
                .build(),
            contentDescription = null,
            modifier = modifier
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
                NutritionDonutChart(nutritionData)
            }
        )
    } else {
        NutritionDonutChart(nutritionData)
    }
}
@Composable
fun NutritionDonutChart(nutritionData: NutritionData) {
    AndroidView(
        modifier = Modifier.size(72.dp),
        factory = { context ->
            PieChart(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                description.isEnabled = false // Hilangkan deskripsi
                isDrawHoleEnabled = true // Buat lubang di tengah (efek donut)
                setHoleColor(android.graphics.Color.TRANSPARENT) // Warna lubang transparan
                holeRadius = 58f // Ukuran lubang (dalam persen)

                legend.isEnabled = false // Hilangkan legend
                setDrawEntryLabels(false) // Hilangkan label di chart

                // Animate chart
                animateY(1000)
            }
        },
        update = { chart ->
            // Siapkan data untuk chart
            val entries = listOf(
                PieEntry(nutritionData.calories.toFloat(), "Calories"),
                PieEntry(nutritionData.carbs.toFloat(), "Carbs"),
                PieEntry(nutritionData.protein.toFloat(), "Protein"),
                PieEntry(nutritionData.totalFat.toFloat(), "Fat"),
                PieEntry(nutritionData.fiber.toFloat(), "Fiber"),
                PieEntry(nutritionData.sugar.toFloat(), "Sugar")
            )

            val colors = listOf(
                caloriesColor.toArgb(),
                carbsColor.toArgb(),
                proteinColor.toArgb(),
                fatColor.toArgb(),
                fiberColor.toArgb(),
                sugarColor.toArgb()
            )

            val dataSet = PieDataSet(entries, "Nutrition").apply {
                this.colors = colors
                setDrawValues(false) // Hilangkan nilai di chart
                sliceSpace = 2f // Jarak antar slice
            }

            val data = PieData(dataSet)
            chart.data = data
            chart.invalidate() // Refresh chart
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
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    val createdAt = try {
        val zonedDateTime = Instant.parse(food.createdAt).atZone(ZoneId.systemDefault())
        "${zonedDateTime.format(dateFormatter)}, ${zonedDateTime.format(timeFormatter)}"
    } catch (e: Exception) {
        "Date not available"
    }

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
                text = food.mealType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(
            text = createdAt,
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