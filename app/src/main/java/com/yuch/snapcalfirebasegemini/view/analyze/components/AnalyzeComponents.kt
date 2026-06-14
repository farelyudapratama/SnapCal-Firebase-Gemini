package com.yuch.snapcalfirebasegemini.view.analyze.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.data.api.response.FoodDetectionByMyModelResult
import com.yuch.snapcalfirebasegemini.data.model.EditableFoodData
import com.yuch.snapcalfirebasegemini.ui.components.food.MealTypeDropdown
import com.yuch.snapcalfirebasegemini.ui.utils.toReadableNutritionSource
import com.yuch.snapcalfirebasegemini.utils.normalizeDecimal
import java.io.File
import java.util.Locale

@Composable
fun AnalyzeLoadingContent(phase: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 5.dp
            )
            Text(
                stringResource(
                    when (phase) {
                        "analyzing" -> R.string.analyzing_your_food
                        "uploading" -> R.string.saving_your_food
                        else -> R.string.analyzing_your_food
                    }
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                stringResource(R.string.this_may_take_a_moment),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AnalyzeServiceSelectionCard(
    selectedService: String?,
    onServiceSelected: (String) -> Unit,
    onAnalyzeClick: (String) -> Unit,
    isLoading: Boolean,
    onMyModelClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Pilih Metode Analisis:",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            AnalyzeMethodCard(
                title = "Analisis dengan Model Yolo",
                description = "Deteksi otomatis menggunakan sistem lokal, lalu dihitung nutrisinya dengan AI.",
                buttonText = "Analisis dengan Model Saya",
                onClick = onMyModelClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                "Atau pilih layanan AI eksternal:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyzeServiceButton(
                    text = "Gemini",
                    description = "Google AI",
                    isSelected = selectedService == "gemini",
                    onClick = { onServiceSelected("gemini") },
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.google_gemini_icon
                )

                AnalyzeServiceButton(
                    text = "🦙 LLaMA",
                    description = "Meta AI",
                    isSelected = selectedService == "groq",
                    onClick = { onServiceSelected("groq") },
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                "Gambar dikirim ke AI eksternal untuk analisis visual dan estimasi nutrisi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Button(
                onClick = { selectedService?.let(onAnalyzeClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedService != null && !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Analisis dengan ${if (selectedService == "gemini") "Gemini" else "LLaMA"}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyzeMethodCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    isRecommended: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )

                if (isRecommended) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            "Terpopuler",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecommended) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        contentColor.copy(alpha = 0.1f)
                    },
                    contentColor = if (isRecommended) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        contentColor
                    }
                )
            ) {
                Text(
                    buttonText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeEditableAnalysisCard(
    foodData: EditableFoodData,
    manualOverrides: MutableMap<String, Boolean>,
    onValueChange: (EditableFoodData) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.food_information),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                stringResource(R.string.adjust_values_if_needed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            NutritionSourceInfo(foodData = foodData)

            Spacer(modifier = Modifier.height(16.dp))
            MealTypeDropdown(
                selectedMealType = foodData.mealType,
                onMealTypeSelected = { onValueChange(foodData.copy(mealType = it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = foodData.foodName,
                onValueChange = { onValueChange(foodData.copy(foodName = it)) },
                label = { Text(stringResource(R.string.food_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            EditableNutritionRow(stringResource(R.string.weight_g), foodData.weightInGrams) {
                onValueChange(foodData.copy(weightInGrams = it.normalizeDecimal()))
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                stringResource(R.string.nutrition_values),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            val nutritionFields = listOf(
                "calories" to stringResource(R.string.nutrient_calories) + " (kcal)",
                "carbs" to stringResource(R.string.nutrient_carbs) + " (g)",
                "protein" to stringResource(R.string.nutrient_protein) + " (g)",
                "totalFat" to stringResource(R.string.nutrient_fat) + " (g)",
                "saturatedFat" to "Saturated Fat (g)",
                "fiber" to stringResource(R.string.nutrient_fiber) + " (g)",
                "sugar" to stringResource(R.string.nutrient_sugar) + " (g)"
            )

            nutritionFields.forEach { (key, label) ->
                ToggleableNutritionRow(
                    label = label,
                    value = foodData.valueForKey(key),
                    isManual = manualOverrides[key] == true,
                    onValueChange = { newValue ->
                        manualOverrides[key] = true
                        onValueChange(foodData.updateField(key, newValue.normalizeDecimal()))
                    },
                    onToggleChange = { isChecked ->
                        manualOverrides[key] = isChecked
                    }
                )
            }
        }
    }
}

@Composable
private fun NutritionSourceInfo(foodData: EditableFoodData) {
    val sourceDetails = foodData.sourceDetails
    val sourceLabel = foodData.sourceType.toReadableNutritionSource()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Nutrition source: $sourceLabel",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            sourceDetails?.estimatedBy?.let {
                Text(
                    text = "Estimated by: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            sourceDetails?.basis?.let {
                Text(
                    text = "Basis: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            sourceDetails?.confidenceNote?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            if (sourceDetails?.requiresUserVerification == true) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Please verify values before saving.",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ToggleableNutritionRow(
    label: String,
    value: String,
    isManual: Boolean,
    onValueChange: (String) -> Unit,
    onToggleChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Edit",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isManual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = isManual,
                    onCheckedChange = onToggleChange,
                    thumbContent = if (isManual) {
                        {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else null
                )
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = isManual,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isManual) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        )
    }
}

@Composable
private fun EditableNutritionRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
fun AnalyzeImagePreview(imagePath: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.height(280.dp)) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = "Food image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun AnalyzeServiceButton(
    text: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconRes: Int? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                )

                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyzeYoloDetectionDialog(
    detections: List<FoodDetectionByMyModelResult>,
    onContinueAnalysis: (selectedFoods: String, description: String?) -> Unit,
    onDirectAiAnalysis: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    var selectedDetections by remember { mutableStateOf(setOf<FoodDetectionByMyModelResult>()) }
    var additionalDescription by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Makanan Terdeteksi",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Pilih makanan yang sesuai dengan gambar Anda:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(detections) { detection ->
                        val isSelected = selectedDetections.contains(detection)

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedDetections = if (isSelected) {
                                    selectedDetections - detection
                                } else {
                                    selectedDetections + detection
                                }
                            },
                            label = {
                                Text(
                                    text = "${detection.foodName} (${String.format(Locale.getDefault(), "%.1f%%", detection.confidence * 100)})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            enabled = true,
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline,
                                selectedBorderColor = Color.Transparent,
                                borderWidth = 1.dp
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = additionalDescription,
                    onValueChange = { additionalDescription = it },
                    label = { Text("Deskripsi Tambahan (Opsional)") },
                    placeholder = { Text("Contoh: dengan nasi, pedas extra, dll.") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDirectAiAnalysis,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Analisis Langsung AI")
                    }

                    Button(
                        onClick = {
                            if (selectedDetections.isNotEmpty()) {
                                val selectedFoodNames = selectedDetections.joinToString(", ") { it.foodName }
                                onContinueAnalysis(
                                    selectedFoodNames,
                                    additionalDescription.takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        enabled = selectedDetections.isNotEmpty() && !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Lanjutkan Analisis")
                        }
                    }
                }
            }
        }
    }
}

private fun EditableFoodData.valueForKey(key: String): String = when (key) {
    "calories" -> calories
    "carbs" -> carbs
    "protein" -> protein
    "totalFat" -> totalFat
    "saturatedFat" -> saturatedFat
    "fiber" -> fiber
    "sugar" -> sugar
    else -> ""
}

private fun EditableFoodData.updateField(field: String, value: String): EditableFoodData = when (field) {
    "calories" -> copy(calories = value)
    "carbs" -> copy(carbs = value)
    "protein" -> copy(protein = value)
    "totalFat" -> copy(totalFat = value)
    "saturatedFat" -> copy(saturatedFat = value)
    "fiber" -> copy(fiber = value)
    "sugar" -> copy(sugar = value)
    else -> this
}
