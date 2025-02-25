package com.yuch.snapcalfirebasegemini.view

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.model.EditableFoodData
import com.yuch.snapcalfirebasegemini.data.model.UpdateFoodData
import com.yuch.snapcalfirebasegemini.viewmodel.FoodViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class)
@Composable
fun EditFoodScreen(
    foodId: String,
    navController: NavController,
    foodItem: FoodItem?, // Data makanan yang akan diedit
    onUpdateFood: (String, String?, UpdateFoodData) -> Unit,
    onBack: () -> Unit,
    viewModel: FoodViewModel
) {
    var foodName by remember { mutableStateOf(foodItem?.foodName ?: "") }
    var mealType by remember { mutableStateOf(foodItem?.mealType ?: "") }
    var calories by remember { mutableStateOf(foodItem?.nutritionData?.calories?.toString() ?: "") }
    var carbs by remember { mutableStateOf(foodItem?.nutritionData?.carbs?.toString() ?: "") }
    var protein by remember { mutableStateOf(foodItem?.nutritionData?.protein?.toString() ?: "") }
    var totalFat by remember { mutableStateOf(foodItem?.nutritionData?.totalFat?.toString() ?: "") }
    var saturatedFat by remember { mutableStateOf(foodItem?.nutritionData?.saturatedFat?.toString() ?: "") }
    var fiber by remember { mutableStateOf(foodItem?.nutritionData?.fiber?.toString() ?: "") }
    var sugar by remember { mutableStateOf(foodItem?.nutritionData?.sugar?.toString() ?: "") }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImagePath = uri.toString()
        }
    }
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Tangani perubahan errorMessage
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearErrorMessage()
            }
        }
    }

    // Tangani perubahan successMessage
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearErrorMessage()
                delay(2000) // Tunda navigasi agar pengguna bisa membaca pesan
                onBack()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Food") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    try {
                        val foodData = UpdateFoodData(
                            foodName = foodName,
                            mealType = mealType,
                            calories = calories.toDoubleOrNull(),
                            carbs = carbs.toDoubleOrNull(),
                            protein = protein.toDoubleOrNull(),
                            totalFat = totalFat.toDoubleOrNull(),
                            saturatedFat = saturatedFat.toDoubleOrNull(),
                            fiber = fiber.toDoubleOrNull(),
                            sugar = sugar.toDoubleOrNull()
                        )

                        onUpdateFood(foodId, selectedImagePath, foodData)
                    } catch (e: Exception) {
                        Log.e("EditFoodScreen", "Error updating food: ${e.message}")
                    }
                }
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Changes")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Gambar makanan
            if (selectedImagePath != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImagePath),
                    contentDescription = "Selected Food Image",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(
                            RoundedCornerShape(8.dp)
                        )
                )
            } else {
                AsyncImage(
                    model = foodItem?.imageUrl,
                    contentDescription = "Food Image",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol pilih gambar
            OutlinedButton(onClick = { launcher.launch("image/*") }) {
                Text("Choose Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nama makanan
            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = { Text("Food Name") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            // Meal Type
            OutlinedTextField(
                value = mealType,
                onValueChange = { mealType = it },
                label = { Text("Meal Type") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            // Input Nutrisi
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                NutrientInputField("Calories", calories) { calories = it }
                NutrientInputField("Carbs", carbs) { carbs = it }
                NutrientInputField("Protein", protein) { protein = it }
                NutrientInputField("Total Fat", totalFat) { totalFat = it }
                NutrientInputField("Saturated Fat", saturatedFat) { saturatedFat = it }
                NutrientInputField("Fiber", fiber) { fiber = it }
                NutrientInputField("Sugar", sugar) { sugar = it }
            }
        }
        if (isLoading) {
            LoadingOverlay()
        }
    }
}

// Fungsi untuk input nutrisi
@Composable
fun NutrientInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Saving food entry...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}