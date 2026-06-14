package com.yuch.snapcalfirebasegemini.ui.components.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yuch.snapcalfirebasegemini.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealTypeDropdown(
    selectedMealType: String?,
    onMealTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    showLabelAbove: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val mealTypes = listOf(
        "breakfast" to Icons.Default.BreakfastDining,
        "lunch" to Icons.Default.LunchDining,
        "dinner" to Icons.Default.DinnerDining,
        "snack" to Icons.Default.Icecream,
        "drink" to Icons.Default.LocalCafe
    )
    val mealTypeLabels = mapOf(
        "breakfast" to stringResource(R.string.breakfast),
        "lunch" to stringResource(R.string.lunch),
        "dinner" to stringResource(R.string.dinner),
        "snack" to stringResource(R.string.snack),
        "drink" to stringResource(R.string.drink)
    )

    Column(modifier = modifier) {
        if (showLabelAbove) {
            Text(
                stringResource(R.string.meal_type),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = mealTypeLabels[selectedMealType] ?: stringResource(R.string.select_meal_type),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.select_meal_type)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    ),
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                mealTypes.forEach { (type, icon) ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(mealTypeLabels[type] ?: type)
                            }
                        },
                        onClick = {
                            onMealTypeSelected(type)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FoodTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    onNext: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val focusModifier = focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        modifier = modifier
            .fillMaxWidth()
            .then(focusModifier),
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNext() }
        ),
        singleLine = true
    )
}

@Composable
fun NutritionField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onNext: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    FoodTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                onValueChange(newValue)
            }
        },
        label = label,
        modifier = modifier.padding(vertical = 8.dp),
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        focusRequester = focusRequester,
        onNext = onNext,
        keyboardType = KeyboardType.Decimal
    )
}

@Composable
fun NutritionFields(
    calories: String,
    carbs: String,
    protein: String,
    totalFat: String,
    saturatedFat: String,
    fiber: String,
    sugar: String,
    onCaloriesChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onTotalFatChange: (String) -> Unit,
    onSaturatedFatChange: (String) -> Unit,
    onFiberChange: (String) -> Unit,
    onSugarChange: (String) -> Unit,
    caloriesFocus: FocusRequester,
    carbsFocus: FocusRequester,
    proteinFocus: FocusRequester,
    totalFatFocus: FocusRequester,
    saturatedFatFocus: FocusRequester,
    fiberFocus: FocusRequester,
    sugarFocus: FocusRequester,
    onDone: () -> Unit
) {
    NutritionField(
        label = stringResource(R.string.nutrient_calories) + " (kcal)",
        value = calories,
        onValueChange = onCaloriesChange,
        focusRequester = caloriesFocus,
        onNext = { carbsFocus.requestFocus() },
        icon = Icons.Default.LocalFireDepartment
    )
    NutritionField(
        label = stringResource(R.string.nutrient_carbs) + " (g)",
        value = carbs,
        onValueChange = onCarbsChange,
        focusRequester = carbsFocus,
        onNext = { proteinFocus.requestFocus() },
        icon = Icons.Default.Grain
    )
    NutritionField(
        label = stringResource(R.string.nutrient_protein) + " (g)",
        value = protein,
        onValueChange = onProteinChange,
        focusRequester = proteinFocus,
        onNext = { totalFatFocus.requestFocus() },
        icon = Icons.Default.FitnessCenter
    )
    NutritionField(
        label = stringResource(R.string.nutrient_fat) + " (g)",
        value = totalFat,
        onValueChange = onTotalFatChange,
        focusRequester = totalFatFocus,
        onNext = { saturatedFatFocus.requestFocus() },
        icon = Icons.Default.WaterDrop
    )
    NutritionField(
        label = "Saturated Fat (g)",
        value = saturatedFat,
        onValueChange = onSaturatedFatChange,
        focusRequester = saturatedFatFocus,
        onNext = { fiberFocus.requestFocus() },
        icon = Icons.Default.WaterDrop
    )
    NutritionField(
        label = stringResource(R.string.nutrient_fiber) + " (g)",
        value = fiber,
        onValueChange = onFiberChange,
        focusRequester = fiberFocus,
        onNext = { sugarFocus.requestFocus() },
        icon = Icons.Default.Grass
    )
    NutritionField(
        label = stringResource(R.string.nutrient_sugar) + " (g)",
        value = sugar,
        onValueChange = onSugarChange,
        focusRequester = sugarFocus,
        onNext = onDone,
        icon = Icons.Default.Cookie
    )
}
