package com.yuch.snapcalfirebasegemini.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel

@OptIn(
    ExperimentalMaterial3Api::class)
@Composable
fun DetailFoodScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    foodId: String,
    onBack: () -> Boolean,
    viewModel: GetFoodViewModel,
) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    val food by viewModel.food.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login"){
                popUpTo(0)
            }
            else -> Unit
        }
    }

    LaunchedEffect(foodId) {
        viewModel.fetchFoodById(foodId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (food != null) {
            AsyncImage(
                model = food?.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
            Text(text = food!!.foodName, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text(text = "Calories: ${food!!.nutritionData.calories}")
            Text(text = "Carbs: ${food!!.nutritionData.carbs}")
            Text(text = "Protein: ${food!!.nutritionData.protein}")
        } else {
            Text(text = "Food not found")
        }
    }

}