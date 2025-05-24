package com.yuch.snapcalfirebasegemini.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.data.api.response.DailySummary
import com.yuch.snapcalfirebasegemini.data.api.response.DailySummaryResponse
import com.yuch.snapcalfirebasegemini.data.api.response.FoodBrief
import com.yuch.snapcalfirebasegemini.data.api.response.Goals
import com.yuch.snapcalfirebasegemini.data.api.response.WeeklySummaryResponse
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NutriTrackScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    viewModel: GetFoodViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login") { popUpTo(0) }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchDailySummary()
        viewModel.fetchWeeklySummary()
    }

    val dailySummary by viewModel.dailySummary.collectAsState()
    val weeklySummary by viewModel.weeklySummary.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NutriTrack Harian") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (dailySummary == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            NutriSummaryContent(
                summary = dailySummary!!,
                weeklySummary = weeklySummary,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun NutriSummaryContent(summary: DailySummaryResponse, modifier: Modifier = Modifier, weeklySummary: WeeklySummaryResponse? = null) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }  // <-- Ngasih space di atas
        item {
            Text("Tanggal: ${summary.date}", style = MaterialTheme.typography.titleMedium)
        }

        item {
            SummaryCard("Ringkasan Nutrisi", summary.data)
        }

        item {
            GoalCard(summary.goals)
        }

        item {
            Text("Makanan Hari Ini", style = MaterialTheme.typography.titleMedium)
        }

        items(items = summary.foods, key = { it.time + it.mealType }) { food ->
            FoodCard(food)
        }

        item {
            Text("", style = MaterialTheme.typography.titleMedium)
        }

        items(summary.feedback) { msg ->
            FeedbackItem(msg)
        }

        if (weeklySummary != null) {
            item {
                WeeklySummaryCard(weeklySummary)
            }
        }
        item { Spacer(modifier = Modifier.height(120.dp)) }  // <-- Ngasih space di bawah
    }
}
@Composable
fun SummaryCard(title: String, data: DailySummary?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDFF5E1))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            if (data != null) {
                Text("Kalori: ${data.totalCalories} kkal")
                Text("Karbohidrat: ${data.totalCarbs} g")
                Text("Protein: ${data.totalProtein} g")
                Text("Lemak Total: ${data.totalFat} g")
                Text("Lemak Jenuh: ${data.totalSaturatedFat} g")
                Text("Serat: ${data.totalFiber} g")
                Text("Gula: ${data.totalSugar} g")
            } else {
                Text("Tidak ada data.")
            }
        }
    }
}

@Composable
fun GoalCard(goals: Goals) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Target Harian", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Text("Kalori: ${goals.calories} kkal")
            Text("Karbohidrat: ${goals.carbs} g")
            Text("Protein: ${goals.protein} g")
            Text("Lemak: ${goals.fat} g")
        }
    }
}
@Composable
fun WeeklySummaryCard(summary: WeeklySummaryResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ringkasan Mingguan", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Periode: ${summary.weekStart} - ${summary.weekEnd}")
            Spacer(Modifier.height(8.dp))

            summary.summaries.forEach { day ->
                Text("📅 ${day.date}")
                Text("Kalori: ${day.calories} kkal")
                Text("Karbo: ${day.carbs} g | Protein: ${day.protein} g | Lemak: ${day.fat} g")
                Text("Serat: ${day.fiber} g | Gula: ${day.sugar} g")
                Spacer(Modifier.height(8.dp))
            }

            Divider()
            Spacer(Modifier.height(8.dp))

            Text("🎯 Target Harian")
            Text("Kalori: ${summary.dailyGoal.calories} kkal")
            Text("Karbo: ${summary.dailyGoal.carbs} g | Protein: ${summary.dailyGoal.protein} g | Lemak: ${summary.dailyGoal.fat} g")
            Text("Serat: ${summary.dailyGoal.fiber} g | Gula: ${summary.dailyGoal.sugar} g")
        }
    }
}

@Composable
fun FoodCard(food: FoodBrief) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Waktu: ${food.time}")
            Text("Tipe Makan: ${food.mealType}")
            Text("Kalori: ${food.calories} kkal")
        }
    }
}

@Composable
fun FeedbackItem(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0E0))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
