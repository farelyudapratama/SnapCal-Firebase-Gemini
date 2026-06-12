package com.yuch.snapcalfirebasegemini.view.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.data.api.response.DailyGoals
import com.yuch.snapcalfirebasegemini.data.api.response.PersonalInfoReq
import com.yuch.snapcalfirebasegemini.ui.components.SectionTitle
import com.yuch.snapcalfirebasegemini.ui.components.SelectableCard
import com.yuch.snapcalfirebasegemini.ui.components.TagChip
import com.yuch.snapcalfirebasegemini.ui.components.TagSelectionSection
import com.yuch.snapcalfirebasegemini.utils.calculateRecommendedCalories
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileFieldError

@Composable
fun WelcomeStep(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.welcome), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.welcome_message),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onGetStarted, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.get_started))
        }
    }
}

@Composable
fun PersonalInfoStep(initialData: PersonalInfoReq?, isEdit: Boolean, fieldErrors: List<ProfileFieldError>, focusRequesters: Map<String, FocusRequester>, onFieldFocus: (String) -> Unit, onDataChange: (PersonalInfoReq) -> Unit) {
    var age by remember { mutableStateOf(initialData?.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(initialData?.gender ?: "") }
    var height by remember { mutableStateOf(initialData?.height?.toString() ?: "") }
    var weight by remember { mutableStateOf(initialData?.weight?.toString() ?: "") }
    var activityLevel by remember { mutableStateOf(initialData?.activityLevel ?: "") }

    LaunchedEffect(age, gender, height, weight, activityLevel) {
        onDataChange(
            PersonalInfoReq(
                age = age.toIntOrNull(),
                gender = gender.ifBlank { null },
                height = height.toIntOrNull(),
                weight = weight.toIntOrNull(),
                activityLevel = activityLevel.ifBlank { null }
            )
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(stringResource(R.string.personal_details))
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text(stringResource(R.string.age)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequesters["age"]!!)
                .onFocusChanged { if (!it.isFocused) onFieldFocus("age") }
                .let { if (fieldErrors.any { error -> error.fieldName == "age" }) it.border(BorderStroke(1.dp, MaterialTheme.colorScheme.error)) else it }
        )
        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text(stringResource(R.string.height_cm)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequesters["height"]!!)
                .onFocusChanged { if (!it.isFocused) onFieldFocus("height") }
                .let { if (fieldErrors.any { error -> error.fieldName == "height" }) it.border(BorderStroke(1.dp, MaterialTheme.colorScheme.error)) else it }
        )
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text(stringResource(R.string.weight_kg)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequesters["weight"]!!)
                .onFocusChanged { if (!it.isFocused) onFieldFocus("weight") }
                .let { if (fieldErrors.any { error -> error.fieldName == "weight" }) it.border(BorderStroke(1.dp, MaterialTheme.colorScheme.error)) else it }
        )

        Text(stringResource(R.string.gender), style = MaterialTheme.typography.bodyLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TagChip(stringResource(R.string.male), gender == "male") { gender = "male" }
            TagChip(stringResource(R.string.female), gender == "female") { gender = "female" }
        }

        Text(stringResource(R.string.activity_level), style = MaterialTheme.typography.bodyLarge)
        OnboardingConstants.activityLevels.forEach { level ->
            SelectableCard(
                title = stringResource(level.titleRes),
                subtitle = stringResource(level.descriptionRes),
                isSelected = activityLevel == level.key,
                onClick = { activityLevel = level.key }
            )
        }
    }
}

@Composable
fun HealthStep(
    selectedConditions: List<String>,
    selectedAllergies: List<String>,
    customConditions: List<String>,
    customAllergies: List<String>,
    onConditionToggle: (String) -> Unit,
    onAllergyToggle: (String) -> Unit,
    onAddCustomCondition: (String) -> Unit,
    onAddCustomAllergy: (String) -> Unit,
    isEdit: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        TagSelectionSection(
            title = stringResource(R.string.health_conditions),
            options = OnboardingConstants.healthConditionOptions,
            selectedItems = selectedConditions,
            customItems = customConditions,
            onToggle = onConditionToggle,
            onAddCustom = onAddCustomCondition,
            getLocalizedText = { OnboardingConstants.getLocalizedHealthCondition(it) }
        )
        TagSelectionSection(
            title = stringResource(R.string.food_allergies),
            options = OnboardingConstants.allergyOptions,
            selectedItems = selectedAllergies,
            customItems = customAllergies,
            onToggle = onAllergyToggle,
            onAddCustom = onAddCustomAllergy,
            getLocalizedText = { OnboardingConstants.getLocalizedAllergy(it) }
        )
    }
}

@Composable
fun DietStep(
    selectedDiets: List<String>,
    customDiets: List<String>,
    onDietToggle: (String) -> Unit,
    onAddCustomDiet: (String) -> Unit,
    isEdit: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TagSelectionSection(
            title = stringResource(R.string.dietary_preferences),
            description = stringResource(R.string.dietary_preferences_description),
            options = OnboardingConstants.dietaryOptions,
            selectedItems = selectedDiets,
            customItems = customDiets,
            onToggle = onDietToggle,
            onAddCustom = onAddCustomDiet,
            getLocalizedText = { OnboardingConstants.getLocalizedDiet(it) }
        )
    }
}

@Composable
fun FoodPreferencesStep(
    likedFoods: List<String>,
    dislikedFoods: List<String>,
    customLikedFoods: List<String>,
    customDislikedFoods: List<String>,
    onLikeToggle: (String) -> Unit,
    onDislikeToggle: (String) -> Unit,
    onAddCustomLiked: (String) -> Unit,
    onAddCustomDisliked: (String) -> Unit,
    isEdit: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        TagSelectionSection(
            title = stringResource(R.string.foods_you_like),
            description = stringResource(R.string.foods_you_like_description),
            options = OnboardingConstants.commonFoods,
            selectedItems = likedFoods,
            customItems = customLikedFoods,
            onToggle = onLikeToggle,
            onAddCustom = onAddCustomLiked,
            getLocalizedText = { OnboardingConstants.getLocalizedFood(it) }
        )
        TagSelectionSection(
            title = stringResource(R.string.foods_you_dislike),
            description = stringResource(R.string.foods_you_dislike_description),
            options = OnboardingConstants.commonFoods,
            selectedItems = dislikedFoods,
            customItems = customDislikedFoods,
            onToggle = onDislikeToggle,
            onAddCustom = onAddCustomDisliked,
            getLocalizedText = { OnboardingConstants.getLocalizedFood(it) }
        )
    }
}

@Composable
fun GoalsStep(
    initialData: DailyGoals?,
    onDataChange: (DailyGoals) -> Unit,
    personalInfo: PersonalInfoReq?,
    isEdit: Boolean,
    fieldErrors: List<ProfileFieldError> = emptyList(),
    focusRequesters: Map<String, FocusRequester> = emptyMap(),
    onFieldFocus: (String) -> Unit = {}
) {
    // State lokal untuk setiap input
    var calories by remember { mutableStateOf(initialData?.calories?.toString() ?: "") }
    var protein by remember { mutableStateOf(initialData?.protein?.toString() ?: "") }
    var carbs by remember { mutableStateOf(initialData?.carbs?.toString() ?: "") }
    var fat by remember { mutableStateOf(initialData?.fat?.toString() ?: "") }
    var fiber by remember { mutableStateOf(initialData?.fiber?.toString() ?: "") }
    var sugar by remember { mutableStateOf(initialData?.sugar?.toString() ?: "") }

    // Memanggil callback saat ada perubahan
    LaunchedEffect(calories, protein, carbs, fat, fiber, sugar) {
        onDataChange(
            DailyGoals(
                calories = calories.toDoubleOrNull(),
                protein = protein.toDoubleOrNull(),
                carbs = carbs.toDoubleOrNull(),
                fat = fat.toDoubleOrNull(),
                fiber = fiber.toDoubleOrNull(),
                sugar = sugar.toDoubleOrNull()
            )
        )
    }

    // Kalkulasi Rekomendasi Kalori (TDEE)
    val recommendedCalories = remember(personalInfo) {
        calculateRecommendedCalories(personalInfo)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(stringResource(R.string.your_daily_goals))
        Text(
            stringResource(R.string.daily_goals_description),
            style = MaterialTheme.typography.bodyMedium
        )

        // Tampilkan Card Rekomendasi
        if (recommendedCalories != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.our_recommendation), fontWeight = FontWeight.Bold)
                        Text("$recommendedCalories ${stringResource(R.string.kcal_per_day)}", style = MaterialTheme.typography.titleLarge)
                    }
                    Button(onClick = { calories = recommendedCalories.toString() }) {
                        Text(stringResource(R.string.use_this))
                    }
                }
            }
        }

        // Form Input untuk setiap Goal dengan validasi dan error highlighting
        val caloriesError = fieldErrors.find { it.fieldName == "calories" }
        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text(stringResource(R.string.calories_kcal)) },
            modifier = Modifier
                .fillMaxWidth()
                .run {
                    if (focusRequesters.containsKey("calories"))
                        this.focusRequester(focusRequesters["calories"]!!)
                    else this
                }
                .onFocusChanged { if (it.isFocused) onFieldFocus("calories") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = caloriesError != null,
            supportingText = {
                if (caloriesError != null) {
                    Text(
                        text = caloriesError.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = protein,
                onValueChange = { protein = it },
                label = { Text(stringResource(R.string.protein_g)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = fieldErrors.any { it.fieldName == "protein" }
            )
            OutlinedTextField(
                value = carbs,
                onValueChange = { carbs = it },
                label = { Text(stringResource(R.string.carbs_g)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = fieldErrors.any { it.fieldName == "carbs" }
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = fat,
                onValueChange = { fat = it },
                label = { Text(stringResource(R.string.fat_g)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = fieldErrors.any { it.fieldName == "fat" }
            )
            OutlinedTextField(
                value = fiber,
                onValueChange = { fiber = it },
                label = { Text(stringResource(R.string.fiber_g)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = fieldErrors.any { it.fieldName == "fiber" }
            )
        }

        // Sugar field dengan error checking
        val sugarError = fieldErrors.find { it.fieldName == "sugar" }
        OutlinedTextField(
            value = sugar,
            onValueChange = { sugar = it },
            label = { Text(stringResource(R.string.sugar_g)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = sugarError != null,
            supportingText = {
                if (sugarError != null) {
                    Text(
                        text = sugarError.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
    }
}

// Bottom Bar Composable dari kode sebelumnya (sedikit modifikasi)
@Composable
fun OnboardingBottomBar(currentStep: Int, totalSteps: Int, onNext: () -> Unit, onFinish: () -> Unit, isLoading: Boolean, isEdit: Boolean) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) { // Tampilkan step counter setelah welcome screen
                Text(stringResource(R.string.step_counter, currentStep, totalSteps - 1), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.weight(1f))
            }

            Button(
                onClick = if (currentStep < totalSteps - 1) onNext else onFinish,
                enabled = !isLoading,
                modifier = Modifier.defaultMinSize(minWidth = 120.dp)
            ) {
                if(isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (currentStep < totalSteps - 1) stringResource(R.string.next) else stringResource(R.string.finish_setup))
                }
            }
        }
    }
}