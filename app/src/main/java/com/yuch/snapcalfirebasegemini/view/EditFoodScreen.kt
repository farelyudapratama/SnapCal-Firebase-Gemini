package com.yuch.snapcalfirebasegemini.view

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.model.UpdateFoodData
import com.yuch.snapcalfirebasegemini.viewmodel.FoodViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel
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
    getFoodViewModel: GetFoodViewModel,
    foodViewModel: FoodViewModel
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
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val imageDeletedMessage by getFoodViewModel.imageDeletedMessage.collectAsState()

    val context = LocalContext.current

//    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//        uri?.let {
//            selectedImageUri = uri.toString()
//        }
//    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
        }
    }


    val isLoading by foodViewModel.isLoading.collectAsState()
    val errorMessage by foodViewModel.errorMessage.collectAsState()
    val successMessage by foodViewModel.successMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Tangani perubahan errorMessage
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                foodViewModel.clearErrorMessage()
            }
        }
    }

    // Tangani perubahan successMessage
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar("$message (Redirecting in 2 seconds)")
                foodViewModel.clearErrorMessage()
                delay(2000)
                navController.previousBackStackEntry?.savedStateHandle?.set("food_updated", true)
                navController.popBackStack()
            }
        }
    }

    LaunchedEffect(imageDeletedMessage) {
        imageDeletedMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                getFoodViewModel.clearImageDeletedMessage()
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
                        val foodData = try {
                            UpdateFoodData(
                                foodName = foodName,
                                mealType = mealType,
                                calories = calories.toDoubleOrNull() ?: 0.0,
                                carbs = carbs.toDoubleOrNull() ?: 0.0,
                                protein = protein.toDoubleOrNull() ?: 0.0,
                                totalFat = totalFat.toDoubleOrNull() ?: 0.0,
                                saturatedFat = saturatedFat.toDoubleOrNull() ?: 0.0,
                                fiber = fiber.toDoubleOrNull() ?: 0.0,
                                sugar = sugar.toDoubleOrNull() ?: 0.0
                            )
                        } catch (e: Exception) {
                            Log.e("EditFoodScreen", "Error parsing food data: ${e.message}")
                            return@FloatingActionButton
                        }

                        onUpdateFood(foodId, selectedImageUri?.let { uriToFile(context, it).absolutePath }, foodData)
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
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
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
            Row {
                val imageAvailable = foodItem?.imageUrl != null || selectedImageUri != null
                val buttonText = if (imageAvailable) "Change Image" else "Choose Image"
                OutlinedButton(onClick = { galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                ) }) {
                    Text(buttonText)
                }
                Spacer(modifier = Modifier.width(8.dp))
                var showDialog by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { showDialog = true },
                    enabled = foodItem?.imageUrl != null,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red,
                        disabledContentColor = Color.Gray
                    ),
                    border = BorderStroke(1.dp, if (foodItem?.imageUrl != null) Color.Red else Color.Gray)
                ) {
                    Text("Delete Image")
                }
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Delete Image") },
                        text = { Text("Are you sure you want to delete this image? This action cannot be undone.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    getFoodViewModel.deleteFoodImageById(foodId)
                                    showDialog = false
                                }
                            ) {
                                Text("Delete", color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
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