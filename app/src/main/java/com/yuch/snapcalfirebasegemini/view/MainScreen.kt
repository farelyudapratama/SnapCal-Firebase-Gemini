@file:OptIn(
    ExperimentalMaterial3Api::class
)

package com.yuch.snapcalfirebasegemini.view

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.ui.components.AnnouncementCarousel
import com.yuch.snapcalfirebasegemini.ui.components.CalendarDialog
import com.yuch.snapcalfirebasegemini.ui.components.DateSelectorCard
import com.yuch.snapcalfirebasegemini.ui.components.EmptyFoodState
import com.yuch.snapcalfirebasegemini.ui.components.FoodItemCard
import com.yuch.snapcalfirebasegemini.ui.components.LoadingAndLoadMore
import com.yuch.snapcalfirebasegemini.ui.components.MainTopAppBar
import com.yuch.snapcalfirebasegemini.ui.navigation.Screen
import com.yuch.snapcalfirebasegemini.viewmodel.AnnouncementViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    foodViewModel: GetFoodViewModel,
    announcementViewModel: AnnouncementViewModel? = null
) {
    val authState = authViewModel.authState.collectAsStateWithLifecycle()
    val email by authViewModel.userEmail.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }

    val foodList by foodViewModel.foodList.collectAsState()
    val isLoading by foodViewModel.isLoading.collectAsState()
    val isLoadingMore by foodViewModel.isLoadingMore.collectAsState()
    val hasMoreData by foodViewModel.hasMoreData.collectAsStateWithLifecycle()
    val errorMessage by foodViewModel.errorMessage.collectAsState()
    
    val announcements = announcementViewModel?.announcements?.collectAsState()?.value ?: emptyList()

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
            announcementViewModel?.fetchAnnouncements()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFB67321), Color(0xFFEA4233)),
                    endY = 400f
                )
            )
    ) {
        Scaffold(
            topBar = {
                MainTopAppBar(email = email.orEmpty(), onRecommendationClick = {
                    navController.navigate(Screen.Recommendation.route)
                })
            },
            containerColor = Color.Transparent,
        ) { paddingValues ->
            // Hanya tampilkan full screen loading untuk initial load dan ketika foodList masih kosong
            if (isLoading && foodList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                // Main Content
                PullToRefreshBox(
                    modifier = Modifier.fillMaxWidth(),
                    state = refreshState,
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        coroutineScope.launch {
                            isRefreshing = true
                            if (isFilterActive && selectedDate != null) {
                                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                val formattedDate = selectedDate!!.format(dateFormatter)
                                foodViewModel.fetchFoodDate(formattedDate)
                            } else {
                                foodViewModel.refreshFood()
                                announcementViewModel?.fetchAnnouncements()
                            }
                            isRefreshing = false
                        }
                    }
                ) {
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
                        
                        if (announcements.isNotEmpty()) {
                            item {
                                AnnouncementCarousel(announcements = announcements)
                            }
                        }
                        
                        item {
                            // Date selector card
                            DateSelectorCard(
                                selectedDate = selectedDate,
                                isFilterActive = isFilterActive,
                                onDateClick = { showCalendarDialog = true },
                                onPreviousDay = {
                                    if (selectedDate != null) {
                                        val newDate = selectedDate!!.minusDays(1)
                                        selectedDate = newDate

                                        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                        val formattedDate = newDate.format(dateFormatter)

                                        foodViewModel.fetchFoodDate(formattedDate)
                                    } else {
                                        val newDate = LocalDateTime.now().minusDays(1)
                                        selectedDate = newDate
                                        isFilterActive = true

                                        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                        val formattedDate = newDate.format(dateFormatter)

                                        foodViewModel.fetchFoodDate(formattedDate)
                                    }
                                },
                                onNextDay = {
                                    if (selectedDate != null) {
                                        val newDate = selectedDate!!.plusDays(1)
                                        selectedDate = newDate

                                        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                        val formattedDate = newDate.format(dateFormatter)

                                        foodViewModel.fetchFoodDate(formattedDate)
                                    } else {
                                        val newDate = LocalDateTime.now().plusDays(1)
                                        selectedDate = newDate
                                        isFilterActive = true

                                        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                        val formattedDate = newDate.format(dateFormatter)

                                        foodViewModel.fetchFoodDate(formattedDate)
                                    }
                                },
                                onClearFilter = {
                                    selectedDate = null
                                    isFilterActive = false
                                    foodViewModel.refreshFood()
                                }
                            )
                        }

                        if (foodList.isEmpty()) {
                            // Empty state dengan styling seperti ProfileScreen
                            item {
                                EmptyFoodState(isFilterActive = isFilterActive)
                            }
                        } else {
                            val groupedFoodList = foodList.groupBy { foodItem ->
                                Instant.parse(foodItem.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
                            }

                            groupedFoodList.forEach { (date, itemsOnDate) ->
                                // Header Tanggal
                                item {
                                    val localDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                                    Text(
                                        text = date.format(localDateFormatter),
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                    )
                                }
                                // List Makanan per Tanggal
                                items(itemsOnDate, key = { it.id }) { foodItem ->
                                    FoodItemCard(foodItem, navController)
                                }
                            }

                            // Load more section
                            if (!isFilterActive) {
                                item {
                                    LoadingAndLoadMore(
                                        isLoading = isLoadingMore,
                                        hasMoreData = hasMoreData,
                                        onLoadMore = { foodViewModel.loadNextPage() }
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }
}
