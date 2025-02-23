package com.yuch.snapcalfirebasegemini.view

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.yuch.snapcalfirebasegemini.viewmodel.*
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    foodViewModel: GetFoodViewModel
) {
    // Observasi state autentikasi
    val authState = authViewModel.authState.observeAsState()

    // Observasi email pengguna (dari AuthViewModel)
    val email by authViewModel.userEmail.observeAsState("")

    // Context
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }

    val foodList by foodViewModel.foodList.collectAsStateWithLifecycle()
    val isLoading by foodViewModel.isLoading.collectAsStateWithLifecycle()
    val hasMoreData by foodViewModel.hasMoreData.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    // Handle back button
    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < 2000) {
            // Jika dalam 2 detik ditekan lagi, keluar aplikasi
            // Keluar aplikasi ketika tekan back di main screen
            android.os.Process.killProcess(android.os.Process.myPid())
        } else {
            // Jika tidak, berikan peringatan dulu
            backPressedTime = currentTime
            Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
        }
    }
    // Redirect ke login jika pengguna tidak terautentikasi
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        PullToRefreshBox(
            modifier = Modifier.padding(8.dp),
            state = state,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    foodViewModel.refreshFood()
                    isRefreshing = false
                }
            },
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = state,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {
            LazyColumn {
                items(foodList) { foodItem ->
                    FoodItemCard(foodItem)
                }
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                item {
                    if (hasMoreData && !isLoading) {
                        Button(
                            onClick = { foodViewModel.loadNextPage() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text("Muat Lebih Banyak")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodItemCard(
    food: FoodItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Image
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(food.imageUrl)
                    .crossfade(true)
                    .transformations(
                        CircleCropTransformation()
                    ) // Biar gambar jadi lingkaran
                    .build(),
                contentDescription = food.foodName,
                loading = {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp)) // Loading indicator
                },
                error = {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = "Error Loading Image",
                        modifier = Modifier.size(60.dp)
                    )
                },
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 16.dp)
            )

            // Food Information
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = food.foodName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Calories with icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Calories",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${food.nutritionData.calories}",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Nutrition Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Carbs
                    NutritionItem(
                        icon = Icons.Default.Grain,
                        value = "${food.nutritionData.carbs}",
                        contentDescription = "Carbohydrates"
                    )

                    // Protein
                    NutritionItem(
                        icon = Icons.Default.FitnessCenter,
                        value = "${food.nutritionData.protein}",
                        contentDescription = "Protein"
                    )

                    // Fat
                    NutritionItem(
                        icon = Icons.Default.Water,
                        value = "${food.nutritionData.totalFat}",
                        contentDescription = "Fat"
                    )

                    // Fiber
                    NutritionItem(
                        icon = Icons.Default.Grass,
                        value = "${food.nutritionData.fiber}",
                        contentDescription = "Fiber"
                    )

                    // Sugar
                    NutritionItem(
                        icon = Icons.Default.Cookie,
                        value = "${food.nutritionData.sugar}",
                        contentDescription = "Sugar"
                    )
                }
            }
        }
    }
}

@Composable
private fun NutritionItem(
    icon: ImageVector,
    value: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}