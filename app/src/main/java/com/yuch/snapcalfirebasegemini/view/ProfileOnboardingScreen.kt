package com.yuch.snapcalfirebasegemini.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.data.api.response.*
import com.yuch.snapcalfirebasegemini.viewmodel.ApiStatus
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.OnboardingViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileViewModel
import java.util.Locale

object OnboardingConstants {
    val allergyOptions = listOf(
        "milk", "eggs", "fish", "shellfish", "tree nuts", "peanuts",
        "wheat", "soybeans", "sesame"
    )

    val healthConditionOptions = listOf(
        "diabetes", "hypertension", "heart disease", "high cholesterol",
        "kidney disease", "liver disease", "thyroid disorder", "arthritis"
    )

    val dietaryOptions = listOf(
        "vegetarian", "vegan", "halal", "kosher", "gluten-free",
        "dairy-free", "low-carb", "keto", "paleo"
    )

    val commonFoods = listOf(
        "chicken", "beef", "fish", "rice", "pasta", "bread", "eggs",
        "milk", "cheese", "yogurt", "apple", "banana", "orange",
        "broccoli", "spinach", "carrot", "potato", "tomato", "onion"
    )

    data class ActivityLevelOption(val key: String, val title: String, val description: String)
    val activityLevels = listOf(
        ActivityLevelOption("sedentary", "Sedentary", "Little to no exercise"),
        ActivityLevelOption("light", "Light", "Light exercise 1-3 days/week"),
        ActivityLevelOption("moderate", "Moderate", "Moderate exercise 3-5 days/week"),
        ActivityLevelOption("active", "Active", "Hard exercise 6-7 days/week"),
        ActivityLevelOption("very-active", "Very Active", "Very hard exercise, physical job")
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileOnboardingScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    onboardingViewModel: OnboardingViewModel
) {
    val currentStep by onboardingViewModel.currentStep.collectAsState()
    val formData by onboardingViewModel.formData.collectAsState()
    val updateStatus by profileViewModel.updateStatus.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Setup Your Profile") },
                navigationIcon = {
                    if (currentStep > 0) {
                        IconButton(onClick = { onboardingViewModel.previousStep() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous Step")
                        }
                    } else {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }
                }
            )
        },
        bottomBar = {
            OnboardingBottomBar(
                currentStep = currentStep,
                totalSteps = onboardingViewModel.totalSteps,
                onNext = { onboardingViewModel.nextStep() },
                onFinish = {
                    profileViewModel.saveOrUpdateProfile(formData) { success ->
                        if (success) {
                            navController.navigate("profile") { popUpTo(0) }
                        }
                    }
                },
                isLoading = updateStatus is ApiStatus.Loading
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            LinearProgressIndicator(
                progress = { (currentStep + 1).toFloat() / onboardingViewModel.totalSteps },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            AnimatedContent(
                targetState = currentStep,
                label = "Onboarding Step Animation",
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                    } else {
                        slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                    }
                }
            ) { step ->
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        when (step) {
                            0 -> WelcomeStep { onboardingViewModel.nextStep() }
                            1 -> PersonalInfoStep(formData.personalInfo) { onboardingViewModel.updatePersonalInfo(it) }
                            2 -> GoalsStep(
                                initialData = formData.dailyGoals,
                                onDataChange = { onboardingViewModel.updateDailyGoals(it) },
                                personalInfo = formData.personalInfo
                            )
                            3 -> HealthStep(
                                selectedConditions = formData.healthConditions,
                                selectedAllergies = formData.allergies,
                                onConditionToggle = { onboardingViewModel.toggleHealthCondition(it) },
                                onAllergyToggle = { onboardingViewModel.toggleAllergy(it) },
                                onAddCustomCondition = { onboardingViewModel.addCustomHealthCondition(it) },
                                onAddCustomAllergy = { onboardingViewModel.addCustomAllergy(it) }
                            )
                            4 -> DietStep(
                                selectedDiets = formData.dietaryRestrictions,
                                onDietToggle = { onboardingViewModel.toggleDietaryRestriction(it) }
                            )
                            5 -> FoodPreferencesStep(
                                likedFoods = formData.likedFoods,
                                dislikedFoods = formData.dislikedFoods,
                                onLikeToggle = { onboardingViewModel.toggleLikedFood(it) },
                                onDislikeToggle = { onboardingViewModel.toggleDislikedFood(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}


// --- Step-Specific Composables ---

@Composable
fun WelcomeStep(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon(Icons.Default.WavingHand, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Text("Welcome!", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            "Let's set up your profile to personalize your nutrition journey. It will only take a minute.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onGetStarted, modifier = Modifier.fillMaxWidth()) {
            Text("Get Started")
        }
    }
}

@Composable
fun PersonalInfoStep(initialData: PersonalInfoReq?, onDataChange: (PersonalInfoReq) -> Unit) {
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
        SectionTitle("Personal Details")
        OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Height (cm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done), modifier = Modifier.fillMaxWidth())

        Text("Gender", style = MaterialTheme.typography.bodyLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TagChip("Male", gender == "male") { gender = "male" }
            TagChip("Female", gender == "female") { gender = "female" }
        }

        Text("Activity Level", style = MaterialTheme.typography.bodyLarge)
        OnboardingConstants.activityLevels.forEach { level ->
            SelectableCard(
                title = level.title,
                subtitle = level.description,
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
    onConditionToggle: (String) -> Unit,
    onAllergyToggle: (String) -> Unit,
    onAddCustomCondition: (String) -> Unit,
    onAddCustomAllergy: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        TagSelectionSection(
            title = "Health Conditions",
            options = OnboardingConstants.healthConditionOptions,
            selectedItems = selectedConditions,
            onToggle = onConditionToggle,
            onAddCustom = onAddCustomCondition
        )
        TagSelectionSection(
            title = "Food Allergies",
            options = OnboardingConstants.allergyOptions,
            selectedItems = selectedAllergies,
            onToggle = onAllergyToggle,
            onAddCustom = onAddCustomAllergy
        )
    }
}

@Composable
fun DietStep(selectedDiets: List<String>, onDietToggle: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Dietary Preferences")
        Text("Select any dietary lifestyles you follow. This helps us filter recipes and suggestions.", style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OnboardingConstants.dietaryOptions.forEach { diet ->
                TagChip(diet.replaceFirstChar { it.titlecase(
                    Locale.ROOT) }, selectedDiets.contains(diet), onClick = { onDietToggle(diet) })
            }
        }
    }
}

@Composable
fun FoodPreferencesStep(
    likedFoods: List<String>,
    dislikedFoods: List<String>,
    onLikeToggle: (String) -> Unit,
    onDislikeToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Foods You Like")
            Text("Tell us what you enjoy eating.", style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OnboardingConstants.commonFoods.forEach { food ->
                    TagChip(food.replaceFirstChar { it.titlecase(Locale.ROOT) }, likedFoods.contains(food), onClick = { onLikeToggle(food) })
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Foods You Dislike")
            Text("Anything you'd rather avoid?", style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OnboardingConstants.commonFoods.forEach { food ->
                    TagChip(food.replaceFirstChar { it.titlecase(Locale.ROOT) }, dislikedFoods.contains(food), onClick = { onDislikeToggle(food) })
                }
            }
        }
    }
}


// --- Reusable UI Components ---

@Composable
fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
}

@Composable
fun TagSelectionSection(
    title: String,
    options: List<String>,
    selectedItems: List<String>,
    onToggle: (String) -> Unit,
    onAddCustom: (String) -> Unit
) {
    var customInput by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { item ->
                TagChip(item.replaceFirstChar { it.titlecase(Locale.ROOT) }, selectedItems.contains(item)) { onToggle(item) }
            }
        }
        OutlinedTextField(
            value = customInput,
            onValueChange = { customInput = it },
            label = { Text("Add other...") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    if (customInput.isNotBlank()) {
                        onAddCustom(customInput)
                        customInput = ""
                    }
                }) {
                    Icon(Icons.Default.Add, "Add")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = if (isSelected) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(FilterChipDefaults.IconSize)) } } else null
    )
}

@Composable
fun SelectableCard(title: String, subtitle: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, "Selected", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
@Composable
fun GoalsStep(
    initialData: DailyGoals?,
    onDataChange: (DailyGoals) -> Unit,
    personalInfo: PersonalInfoReq? // Menerima data dari langkah sebelumnya
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
                calories = calories.toIntOrNull(),
                protein = protein.toIntOrNull(),
                carbs = carbs.toIntOrNull(),
                fat = fat.toIntOrNull(),
                fiber = fiber.toIntOrNull(),
                sugar = sugar.toIntOrNull()
            )
        )
    }

    // Kalkulasi Rekomendasi Kalori (TDEE)
    val recommendedCalories = remember(personalInfo) {
        calculateRecommendedCalories(personalInfo)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Your Daily Goals")
        Text(
            "Set your daily nutrition targets. You can use our recommendation or set your own.",
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
                        Text("Our Recommendation", fontWeight = FontWeight.Bold)
                        Text("$recommendedCalories kcal / day", style = MaterialTheme.typography.titleLarge)
                    }
                    Button(onClick = { calories = recommendedCalories.toString() }) {
                        Text("Use This")
                    }
                }
            }
        }

        // Form Input untuk setiap Goal
        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text("Calories (kcal)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = protein, onValueChange = { protein = it }, label = { Text("Protein (g)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("Carbs (g)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = fat, onValueChange = { fat = it }, label = { Text("Fat (g)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = fiber, onValueChange = { fiber = it }, label = { Text("Fiber (g)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        }
    }
}

// Fungsi helper untuk kalkulasi TDEE, bisa ditaruh di file terpisah (misal: util/NutritionCalculator.kt)
fun calculateRecommendedCalories(info: PersonalInfoReq?): Int? {
    if (info?.age == null || info.height == null || info.weight == null || info.gender == null || info.activityLevel == null) {
        return null // Tidak bisa menghitung jika data tidak lengkap
    }

    // Formula Harris-Benedict untuk BMR
    val bmr = if (info.gender.lowercase() == "male") {
        88.362 + (13.397 * info.weight) + (4.799 * info.height) - (5.677 * info.age)
    } else { // female
        447.593 + (9.247 * info.weight) + (3.098 * info.height) - (4.330 * info.age)
    }

    // Multiplier TDEE berdasarkan tingkat aktivitas
    val activityMultiplier = when (info.activityLevel) {
        "sedentary" -> 1.2
        "light" -> 1.375
        "moderate" -> 1.55
        "active" -> 1.725
        "very-active" -> 1.9
        else -> 1.2
    }

    // TDEE = BMR * Activity Multiplier
    return (bmr * activityMultiplier).toInt()
}
// Bottom Bar Composable dari kode sebelumnya (sedikit modifikasi)
@Composable
fun OnboardingBottomBar(currentStep: Int, totalSteps: Int, onNext: () -> Unit, onFinish: () -> Unit, isLoading: Boolean) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) { // Tampilkan step counter setelah welcome screen
                Text("Step ${currentStep} of ${totalSteps - 1}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
                    Text(if (currentStep < totalSteps - 1) "Next" else "Finish Setup")
                }
            }
        }
    }
}