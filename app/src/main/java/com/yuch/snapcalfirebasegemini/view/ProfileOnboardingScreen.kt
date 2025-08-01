package com.yuch.snapcalfirebasegemini.view

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileFieldError
import com.yuch.snapcalfirebasegemini.viewmodel.ApiStatus
import kotlinx.coroutines.launch
import com.yuch.snapcalfirebasegemini.data.api.response.*
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.OnboardingViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.toProfileRequest
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

    data class ActivityLevelOption(val key: String, val titleRes: Int, val descriptionRes: Int)
    val activityLevels = listOf(
        ActivityLevelOption("sedentary", R.string.activity_sedentary, R.string.activity_sedentary_desc),
        ActivityLevelOption("light", R.string.activity_light, R.string.activity_light_desc),
        ActivityLevelOption("moderate", R.string.activity_moderate, R.string.activity_moderate_desc),
        ActivityLevelOption("active", R.string.activity_active, R.string.activity_active_desc),
        ActivityLevelOption("very-active", R.string.activity_very_active, R.string.activity_very_active_desc)
    )

    // Helper functions to get localized text for options
    fun getLocalizedHealthCondition(condition: String): Int = when (condition) {
        "diabetes" -> R.string.condition_diabetes
        "hypertension" -> R.string.condition_hypertension
        "heart disease" -> R.string.condition_heart_disease
        "high cholesterol" -> R.string.condition_high_cholesterol
        "kidney disease" -> R.string.condition_kidney_disease
        "liver disease" -> R.string.condition_liver_disease
        "thyroid disorder" -> R.string.condition_thyroid_disorder
        "arthritis" -> R.string.condition_arthritis
        else -> R.string.condition_diabetes // fallback
    }

    fun getLocalizedAllergy(allergy: String): Int = when (allergy) {
        "milk" -> R.string.allergy_milk
        "eggs" -> R.string.allergy_eggs
        "fish" -> R.string.allergy_fish
        "shellfish" -> R.string.allergy_shellfish
        "tree nuts" -> R.string.allergy_tree_nuts
        "peanuts" -> R.string.allergy_peanuts
        "wheat" -> R.string.allergy_wheat
        "soybeans" -> R.string.allergy_soybeans
        "sesame" -> R.string.allergy_sesame
        else -> R.string.allergy_milk // fallback
    }

    fun getLocalizedDiet(diet: String): Int = when (diet) {
        "vegetarian" -> R.string.diet_vegetarian
        "vegan" -> R.string.diet_vegan
        "halal" -> R.string.diet_halal
        "kosher" -> R.string.diet_kosher
        "gluten-free" -> R.string.diet_gluten_free
        "dairy-free" -> R.string.diet_dairy_free
        "low-carb" -> R.string.diet_low_carb
        "keto" -> R.string.diet_keto
        "paleo" -> R.string.diet_paleo
        else -> R.string.diet_vegetarian // fallback
    }

    fun getLocalizedFood(food: String): Int = when (food) {
        "chicken" -> R.string.food_chicken
        "beef" -> R.string.food_beef
        "fish" -> R.string.food_fish
        "rice" -> R.string.food_rice
        "pasta" -> R.string.food_pasta
        "bread" -> R.string.food_bread
        "eggs" -> R.string.food_eggs
        "milk" -> R.string.food_milk
        "cheese" -> R.string.food_cheese
        "yogurt" -> R.string.food_yogurt
        "apple" -> R.string.food_apple
        "banana" -> R.string.food_banana
        "orange" -> R.string.food_orange
        "broccoli" -> R.string.food_broccoli
        "spinach" -> R.string.food_spinach
        "carrot" -> R.string.food_carrot
        "potato" -> R.string.food_potato
        "tomato" -> R.string.food_tomato
        "onion" -> R.string.food_onion
        else -> R.string.food_chicken // fallback
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileOnboardingScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    onboardingViewModel: OnboardingViewModel,
    isEdit: Boolean
) {
    val currentStep by onboardingViewModel.currentStep.collectAsState()
    val formData by onboardingViewModel.formData.collectAsState()
    val updateStatus by profileViewModel.updateStatus.collectAsState()
    val userPreferences by profileViewModel.userPreferences.collectAsState()
    val alreadyLoaded = remember { mutableStateOf(false) }
    val fieldErrors by profileViewModel.fieldErrors.collectAsState()

    // Membuat snackbarHostState untuk menampilkan Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Error handling state
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Koleksi focus requesters untuk setiap field yang bisa error
    val focusRequesters = remember {
        mapOf(
            "age" to FocusRequester(),
            "height" to FocusRequester(),
            "weight" to FocusRequester(),
            "gender" to FocusRequester(),
            "activityLevel" to FocusRequester(),
            "calories" to FocusRequester()
        )
    }

    if (isEdit) {
        LaunchedEffect(isEdit) {
            // Refresh data dari server
            profileViewModel.refreshProfile()

            // Tunggu sampai userPreferences tidak null
            profileViewModel.userPreferences.collect { prefs ->
                if (prefs != null && !alreadyLoaded.value) {
                    val profileRequest = prefs.toProfileRequest()
                    onboardingViewModel.loadProfile(profileRequest)
                    alreadyLoaded.value = true
                }
            }
        }
    }

    // Monitor perubahan updateStatus untuk menampilkan Snackbar saat error
    LaunchedEffect(updateStatus) {
        if (updateStatus is ApiStatus.Error) {
            val errorMsg = (updateStatus as ApiStatus.Error).message
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMsg,
                    actionLabel = "OK",
                    duration = SnackbarDuration.Long
                )

                // Jika ada field errors, coba fokus ke field pertama yang error
                if (fieldErrors.isNotEmpty()) {
                    val firstErrorField = fieldErrors.first().fieldName
                    focusRequesters[firstErrorField]?.let { requester ->
                        try {
                            requester.requestFocus()
                        } catch (e: Exception) {
                            // Log error jika terjadi masalah dengan focus requester
                            Log.e("ProfileOnboardingScreen", "Failed to request focus: ${e.message}")
                        }
                    }
                }
            }
        } else if (updateStatus is ApiStatus.Success && !isEdit) {
            // Tampilkan snackbar sukses HANYA jika bukan dalam mode edit initial load
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = (updateStatus as ApiStatus.Success).message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Tambahkan LaunchedEffect untuk mode edit
    LaunchedEffect(isEdit, alreadyLoaded.value) {
        if (isEdit && alreadyLoaded.value) {
            // Tampilkan snackbar hanya sekali setelah data dimuat
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Mode edit aktif. Profil siap untuk diedit.",
                    actionLabel = "OK",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEdit) stringResource(R.string.edit_your_profile) else stringResource(R.string.setup_your_profile),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (isEdit) {
                            Text(
                                text = stringResource(R.string.editing_mode),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (currentStep > 0) {
                        IconButton(onClick = { onboardingViewModel.previousStep() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.previous_step))
                        }
                    } else {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.Close, stringResource(R.string.close))
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (isEdit) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    titleContentColor = if (isEdit) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
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
                isLoading = updateStatus is ApiStatus.Loading,
                isEdit = isEdit
            )
        },
        containerColor = if (isEdit) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Edit mode indicator banner
            if (isEdit) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.edit_mode_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Jika ada field errors, tampilkan banner peringatan
            if (fieldErrors.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Ada beberapa error yang perlu diperbaiki",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = fieldErrors.joinToString(", ") { it.errorMessage },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            LinearProgressIndicator(
                progress = { (currentStep + 1).toFloat() / onboardingViewModel.totalSteps },
                modifier = Modifier.fillMaxWidth(),
                color = if (isEdit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                trackColor = if (isEdit) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
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
                            0 -> if (isEdit) {
                                EditWelcomeStep { onboardingViewModel.nextStep() }
                            } else {
                                WelcomeStep { onboardingViewModel.nextStep() }
                            }
                            1 -> PersonalInfoStep(
                                formData.personalInfo,
                                isEdit,
                                fieldErrors.filter { it.fieldName in listOf("age", "height", "weight", "gender", "activityLevel") },
                                focusRequesters,
                                onFieldFocus = { profileViewModel.clearFieldError(it) },
                                onDataChange = { onboardingViewModel.updatePersonalInfo(it) }
                            )
                            2 -> GoalsStep(
                                initialData = formData.dailyGoals?.let {
                                    DailyGoals(
                                        calories = it.calories,
                                        protein = it.protein,
                                        carbs = it.carbs,
                                        fat = it.fat,
                                        fiber = it.fiber,
                                        sugar = it.sugar
                                    )
                                },
                                onDataChange = { onboardingViewModel.updateDailyGoals(it) },
                                personalInfo = formData.personalInfo,
                                isEdit = isEdit,
                                fieldErrors = fieldErrors.filter { it.fieldName in listOf("calories", "protein", "carbs", "fat", "fiber", "sugar") },
                                focusRequesters = focusRequesters,
                                onFieldFocus = { profileViewModel.clearFieldError(it) }
                            )
                            3 -> HealthStep(
                                selectedConditions = formData.healthConditions,
                                selectedAllergies = formData.allergies,
                                customConditions = formData.customHealthConditions,
                                customAllergies = formData.customAllergies,
                                onConditionToggle = { onboardingViewModel.toggleHealthCondition(it) },
                                onAllergyToggle = { onboardingViewModel.toggleAllergy(it) },
                                onAddCustomCondition = { onboardingViewModel.addCustomHealthCondition(it) },
                                onAddCustomAllergy = { onboardingViewModel.addCustomAllergy(it) },
                                isEdit = isEdit
                            )
                            4 -> DietStep(
                                selectedDiets = formData.dietaryRestrictions,
                                customDiets = formData.customDietaryRestrictions,
                                onDietToggle = { onboardingViewModel.toggleDietaryRestriction(it) },
                                onAddCustomDiet = { onboardingViewModel.addCustomDietaryRestriction(it) },
                                isEdit = isEdit
                            )
                            5 -> FoodPreferencesStep(
                                likedFoods = formData.likedFoods,
                                dislikedFoods = formData.dislikedFoods,
                                customLikedFoods = formData.customLikedFoods,
                                customDislikedFoods = formData.customDislikedFoods,
                                onLikeToggle = { onboardingViewModel.toggleLikedFood(it) },
                                onDislikeToggle = { onboardingViewModel.toggleDislikedFood(it) },
                                onAddCustomLiked = { onboardingViewModel.addCustomLikedFood(it) },
                                onAddCustomDisliked = { onboardingViewModel.addCustomDislikedFood(it) },
                                isEdit = isEdit
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
fun EditWelcomeStep(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.welcome_back), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.welcome_back_message),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onGetStarted, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.continue_editing))
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


// --- Reusable UI Components ---

@Composable
fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelectionSection(
    title: String,
    options: List<String>,
    selectedItems: List<String>,
    customItems: List<String> = emptyList(),
    onToggle: (String) -> Unit,
    onAddCustom: (String) -> Unit,
    getLocalizedText: (String) -> Int,
    description: String? = null
) {
    var customInput by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title)
        description?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Tampilkan predefined options
            options.forEach { item ->
                TagChip(stringResource(getLocalizedText(item)), selectedItems.contains(item)) { onToggle(item) }
            }
            // Tampilkan custom items
            customItems.forEach { item ->
                TagChip(item.replaceFirstChar { it.titlecase(Locale.getDefault()) }, selectedItems.contains(item)) { onToggle(item) }
            }
        }
        OutlinedTextField(
            value = customInput,
            onValueChange = { customInput = it },
            label = { Text(stringResource(R.string.add_other)) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    if (customInput.isNotBlank()) {
                        onAddCustom(customInput.trim())
                        customInput = ""
                    }
                }) {
                    Icon(Icons.Default.Add, stringResource(R.string.add))
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