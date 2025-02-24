package com.yuch.snapcalfirebasegemini.view

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class)
@Composable
fun EditFoodScreen(
    foodId: String,
    navController: NavController,
    foodItem: FoodItem?, // Data makanan yang akan diedit
    onUpdateFood: (String, String?, EditableFoodData) -> Unit,
    onBack: () -> Unit
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Food") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val foodData = EditableFoodData(
                        foodName = foodName,
                        mealType = mealType,
                        calories = (calories.toIntOrNull() ?: 0).toString(),
                        carbs = (carbs.toIntOrNull() ?: 0).toString(),
                        protein = (protein.toIntOrNull() ?: 0).toString(),
                        totalFat = (totalFat.toIntOrNull() ?: 0).toString(),
                        saturatedFat = (saturatedFat.toIntOrNull() ?: 0).toString(),
                        fiber = (fiber.toIntOrNull() ?: 0).toString(),
                        sugar = (sugar.toIntOrNull() ?: 0).toString()
                    )
                    Log.d("EditFoodScreen", "Save changes clicked $foodData")
                    onUpdateFood(foodId, selectedImagePath, foodData)
                    navController.popBackStack()
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
