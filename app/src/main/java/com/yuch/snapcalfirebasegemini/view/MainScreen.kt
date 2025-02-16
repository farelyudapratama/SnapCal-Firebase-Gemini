package com.yuch.snapcalfirebasegemini.view

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
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
    foodViewModel: FoodViewModel
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
    val onRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            foodViewModel.fetchFood()
            isRefreshing = false
        }
    }

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

    Column(modifier.fillMaxSize().padding(16.dp)) {
        PullToRefreshBox(
            modifier = Modifier.padding(8.dp),
            state = state,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = state,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
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
                // Jika masih ada data (halaman belum selesai), tampilkan tombol load more
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
    food: FoodItem
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = food.foodName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Kalori: ${food.nutritionData.calories}")
        }
    }
}