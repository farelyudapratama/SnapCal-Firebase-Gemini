@file:OptIn(
    ExperimentalMaterial3Api::class
)

package com.yuch.snapcalfirebasegemini.view

import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DateRange
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.yuch.snapcalfirebasegemini.R
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
    val errorMessage by foodViewModel.errorMessage.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    // Calendar selection state
    var selectedDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var showCalendarDialog by remember { mutableStateOf(false) }

    // Filter state
    var isFilterActive by remember { mutableStateOf(false) }

    // Handle back button and auth state
    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < 2000) {
            (context as? android.app.Activity)?.finishAffinity()
        } else {
            backPressedTime = currentTime
            Toast.makeText(context,
                context.getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show()
        }
    }

    // Show error messages if any
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
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

    // Calendar dialog
    if (showCalendarDialog) {
        CalendarDialog(
            selectedDate = selectedDate ?: LocalDateTime.now(),
            onDateSelected = { date ->
                selectedDate = date
                isFilterActive = true

                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val formattedDate = date.format(dateFormatter)

                foodViewModel.fetchFoodDate(formattedDate)

                showCalendarDialog = false
            },
            onDismiss = { showCalendarDialog = false }
        )
    }

        Column(
            modifier = modifier
                .fillMaxSize()
//                .padding(paddingValues)
        ) {
            TopBar(
                selectedDate = selectedDate,
                isFilterActive = isFilterActive,
                onDateClick = { showCalendarDialog = true },
                onPreviousDay = {
                    if (selectedDate != null) {
                        val newDate = selectedDate!!.minusDays(1)
                        selectedDate = newDate

                        // Format date
                        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val formattedDate = newDate.format(dateFormatter)

                        // Fetch food for previous day
                        foodViewModel.fetchFoodDate(formattedDate)
                    } else {
                        val newDate = LocalDateTime.now().minusDays(1)
                        selectedDate = newDate
                        isFilterActive = true

                        // Format date
                        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val formattedDate = newDate.format(dateFormatter)

                        // Fetch food for previous day
                        foodViewModel.fetchFoodDate(formattedDate)
                    }
                },
                onNextDay = {
                    if (selectedDate != null) {
                        val newDate = selectedDate!!.plusDays(1)
                        selectedDate = newDate

                        // Format date
                        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val formattedDate = newDate.format(dateFormatter)

                        // Fetch food for next day
                        foodViewModel.fetchFoodDate(formattedDate)
                    } else {
                        val newDate = LocalDateTime.now().plusDays(1)
                        selectedDate = newDate
                        isFilterActive = true

                        // Format date
                        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val formattedDate = newDate.format(dateFormatter)

                        // Fetch food for next day
                        foodViewModel.fetchFoodDate(formattedDate)
                    }
                },
                onClearFilter = {
                    selectedDate = null
                    isFilterActive = false
                    // Refresh to get all data
                    foodViewModel.refreshFood()
                },
                email = email
            )

            // Main Content
            PullToRefreshBox(
                modifier = Modifier.fillMaxWidth(),
                state = refreshState,
                isRefreshing = isRefreshing,
                onRefresh = {
                    coroutineScope.launch {
                        isRefreshing = true
                        if (isFilterActive && selectedDate != null) {
                            // Format date
                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            val formattedDate = selectedDate!!.format(dateFormatter)

                            // Refresh with date filter
                            foodViewModel.fetchFoodDate(formattedDate)
                        } else {
                            // Refresh all data
                            foodViewModel.refreshFood()
                        }
                        isRefreshing = false
                    }
                }
            ) {
                if (foodList.isEmpty() && !isLoading) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NoFood,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (isFilterActive) stringResource(R.string.no_food_on_date) else stringResource(R.string.empty_food_entry),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    FoodListByDate(foodList, navController, isFilterActive, isLoading, hasMoreData, foodViewModel)
                }
            }
        }

}

@Composable
fun TopBar(
    selectedDate: LocalDateTime?,
    isFilterActive: Boolean,
    onDateClick: () -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onClearFilter: () -> Unit,
    email: String
) {
    Column(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        // App title and email
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

        // Calendar date selector bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousDay,
                    enabled = true // Always enabled for better UX
                ) {
                    Icon(
                        imageVector = Icons.Default.NavigateBefore,
                        contentDescription = "Previous Day"
                    )
                }

                Row(
                    modifier = Modifier
                        .clickable(onClick = onDateClick)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = "Calendar",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (isFilterActive && selectedDate != null) {
                        val locale = Locale.getDefault()
                        val pattern = if (locale.language == "id") {
                            "EEEE, d MMMM yyyy"
                        } else {
                            "EEEE, MMMM d, yyyy"
                        }

                        val dateFormatter = DateTimeFormatter.ofPattern(pattern, locale)
                        Text(
                            text = selectedDate.format(dateFormatter),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onClearFilter,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Filter",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.all_entries),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onNextDay,
                    enabled = true
                ) {
                    Icon(
                        imageVector = Icons.Default.NavigateNext,
                        contentDescription = "Next Day"
                    )
                }
            }
        }
    }
}

@Composable
fun FoodListByDate(
    foodList: List<FoodItem>,
    navController: NavController,
    isFilterActive: Boolean,
    isLoading: Boolean,
    hasMoreData: Boolean,
    foodViewModel: GetFoodViewModel
) {
    val groupedFoodList = foodList.groupBy { foodItem ->
        val date = Instant.parse(foodItem.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
        date
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedFoodList.forEach { (date, foodItems) ->
            item {
                val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                Text(
                    text = date.format(dateFormatter),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(foodItems) { foodItem ->
                FoodItemCard(foodItem, navController)
            }
        }
        // Loading and Load More
        item {
            if (!isFilterActive) {
                LoadingAndLoadMore(
                    isLoading = isLoading,
                    hasMoreData = hasMoreData,
                    onLoadMore = { foodViewModel.loadNextPage() }
                )
            } else if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDialog(
    selectedDate: LocalDateTime,
    onDateSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val newDate = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(millis),
                        ZoneId.systemDefault()
                    ).withHour(selectedDate.hour)
                        .withMinute(selectedDate.minute)
                    onDateSelected(newDate)
                }
            }) {
                Text(stringResource(R.string.text_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.text_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
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
        NutritionItem(Icons.Default.Grain, "${nutritionData.carbs}g", stringResource(R.string.nutrient_carbs))
        NutritionItem(Icons.Default.FitnessCenter, "${nutritionData.protein}g", stringResource(R.string.nutrient_protein))
        NutritionItem(Icons.Default.Water, "${nutritionData.totalFat}g", stringResource(R.string.nutrient_fat))
        NutritionItem(Icons.Default.Grass, "${nutritionData.fiber}g", stringResource(R.string.nutrient_fiber))
        NutritionItem(Icons.Default.Cookie, "${nutritionData.sugar}g", stringResource(R.string.nutrient_sugar))
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
        stringResource(R.string.date_not_available)
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
                Text(stringResource(R.string.load_more))
            }
        }
    }
}