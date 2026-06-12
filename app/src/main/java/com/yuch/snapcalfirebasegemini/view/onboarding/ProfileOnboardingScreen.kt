package com.yuch.snapcalfirebasegemini.view.onboarding

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.ui.navigation.Screen
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.OnboardingViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileState
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.toProfileRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileOnboardingScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    onboardingViewModel: OnboardingViewModel,
    isEdit: Boolean
) {
    val currentStep by onboardingViewModel.currentStep.collectAsState()
    val formData by onboardingViewModel.formData.collectAsState()
    val updateStatus by profileViewModel.updateStatus.collectAsState()
    val alreadyLoaded = remember { mutableStateOf(false) }
    val fieldErrors by profileViewModel.fieldErrors.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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
            profileViewModel.refreshProfile()
            profileViewModel.userPreferences.collect { prefs ->
                if (prefs != null && !alreadyLoaded.value) {
                    val profileRequest = prefs.toProfileRequest()
                    onboardingViewModel.loadProfile(profileRequest)
                    alreadyLoaded.value = true
                }
            }
        }
    }

    LaunchedEffect(updateStatus) {
        if (updateStatus is ProfileState.Error) {
            val errorMsg = (updateStatus as ProfileState.Error).message
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = errorMsg, actionLabel = "OK", duration = SnackbarDuration.Long)
                if (fieldErrors.isNotEmpty()) {
                    val firstErrorField = fieldErrors.first().fieldName
                    focusRequesters[firstErrorField]?.let { requester ->
                        try { requester.requestFocus() } catch (e: Exception) { Log.e("ProfileScreen", "Focus fail: ${e.message}") }
                    }
                }
            }
        } else if (updateStatus is ProfileState.Success && !isEdit) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = (updateStatus as ProfileState.Success).message, duration = SnackbarDuration.Short)
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
                    if (!isEdit && currentStep > 0) {
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
                    containerColor = if (isEdit) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (isEdit) {
                // Bottom bar khusus mode edit (Satu tombol simpan)
                Surface(shadowElevation = 8.dp) {
                    Button(
                        onClick = {
                            profileViewModel.saveOrUpdateProfile(formData) { success ->
                                if (success) navController.navigate(Screen.Profile.route) { popUpTo(0) }
                            }
                        },
                        enabled = updateStatus !is ProfileState.Loading,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        if (updateStatus is ProfileState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Simpan Perubahan")
                        }
                    }
                }
            } else {
                // Bottom bar wizard untuk user baru
                OnboardingBottomBar(
                    currentStep = currentStep,
                    totalSteps = onboardingViewModel.totalSteps,
                    onNext = { onboardingViewModel.nextStep() },
                    onFinish = {
                        profileViewModel.saveOrUpdateProfile(formData) { success ->
                            if (success) navController.navigate(Screen.Profile.route) { popUpTo(0) }
                        }
                    },
                    isLoading = updateStatus is ProfileState.Loading,
                    isEdit = false
                )
            }
        },
        containerColor = if (isEdit) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f) else MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            if (isEdit) {
                // ==========================================
                // MODE 1: LONG SCROLL UNTUK EDIT PROFIL
                // ==========================================
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    item {
                        PersonalInfoStep(
                            initialData = formData.personalInfo,
                            isEdit = true,
                            fieldErrors = fieldErrors.filter { it.fieldName in listOf("age", "height", "weight", "gender", "activityLevel") },
                            focusRequesters = focusRequesters,
                            onFieldFocus = { profileViewModel.clearFieldError(it) },
                            onDataChange = { onboardingViewModel.updatePersonalInfo(it) }
                        )
                    }
                    item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }
                    item {
                        GoalsStep(
                            initialData = formData.dailyGoals,
                            onDataChange = { onboardingViewModel.updateDailyGoals(it) },
                            personalInfo = formData.personalInfo,
                            isEdit = true,
                            fieldErrors = fieldErrors.filter { it.fieldName in listOf("calories", "protein", "carbs", "fat", "fiber", "sugar") },
                            focusRequesters = focusRequesters,
                            onFieldFocus = { profileViewModel.clearFieldError(it) }
                        )
                    }
                    item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }
                    item {
                        HealthStep(
                            selectedConditions = formData.healthConditions,
                            selectedAllergies = formData.allergies,
                            customConditions = formData.customHealthConditions,
                            customAllergies = formData.customAllergies,
                            onConditionToggle = { onboardingViewModel.toggleHealthCondition(it) },
                            onAllergyToggle = { onboardingViewModel.toggleAllergy(it) },
                            onAddCustomCondition = { onboardingViewModel.addCustomHealthCondition(it) },
                            onAddCustomAllergy = { onboardingViewModel.addCustomAllergy(it) },
                            isEdit = true
                        )
                    }
                    item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }
                    item {
                        DietStep(
                            selectedDiets = formData.dietaryRestrictions,
                            customDiets = formData.customDietaryRestrictions,
                            onDietToggle = { onboardingViewModel.toggleDietaryRestriction(it) },
                            onAddCustomDiet = { onboardingViewModel.addCustomDietaryRestriction(it) },
                            isEdit = true
                        )
                    }
                    item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }
                    item {
                        FoodPreferencesStep(
                            likedFoods = formData.likedFoods,
                            dislikedFoods = formData.dislikedFoods,
                            customLikedFoods = formData.customLikedFoods,
                            customDislikedFoods = formData.customDislikedFoods,
                            onLikeToggle = { onboardingViewModel.toggleLikedFood(it) },
                            onDislikeToggle = { onboardingViewModel.toggleDislikedFood(it) },
                            onAddCustomLiked = { onboardingViewModel.addCustomLikedFood(it) },
                            onAddCustomDisliked = { onboardingViewModel.addCustomDislikedFood(it) },
                            isEdit = true
                        )
                    }
                }

            } else {
                // ==========================================
                // MODE 2: MULTI-STEP WIZARD UNTUK USER BARU
                // ==========================================
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    LinearProgressIndicator(
                        progress = { (currentStep + 1).toFloat() / onboardingViewModel.totalSteps },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    )

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
                                    1 -> PersonalInfoStep(
                                        initialData = formData.personalInfo,
                                        isEdit = false,
                                        fieldErrors = fieldErrors.filter { it.fieldName in listOf("age", "height", "weight", "gender", "activityLevel") },
                                        focusRequesters = focusRequesters,
                                        onFieldFocus = { profileViewModel.clearFieldError(it) },
                                        onDataChange = { onboardingViewModel.updatePersonalInfo(it) }
                                    )
                                    2 -> GoalsStep(
                                        initialData = formData.dailyGoals,
                                        onDataChange = { onboardingViewModel.updateDailyGoals(it) },
                                        personalInfo = formData.personalInfo,
                                        isEdit = false,
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
                                        isEdit = false
                                    )
                                    4 -> DietStep(
                                        selectedDiets = formData.dietaryRestrictions,
                                        customDiets = formData.customDietaryRestrictions,
                                        onDietToggle = { onboardingViewModel.toggleDietaryRestriction(it) },
                                        onAddCustomDiet = { onboardingViewModel.addCustomDietaryRestriction(it) },
                                        isEdit = false
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
                                        isEdit = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}