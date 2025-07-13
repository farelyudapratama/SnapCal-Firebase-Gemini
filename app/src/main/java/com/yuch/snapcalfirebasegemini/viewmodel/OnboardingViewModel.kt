// File: viewmodel/OnboardingViewModel.kt
package com.yuch.snapcalfirebasegemini.viewmodel

import androidx.lifecycle.ViewModel
import com.yuch.snapcalfirebasegemini.data.api.response.DailyGoals
import com.yuch.snapcalfirebasegemini.data.api.response.PersonalInfoReq
import com.yuch.snapcalfirebasegemini.data.api.response.ProfileRequest
import com.yuch.snapcalfirebasegemini.data.api.response.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

//class OnboardingViewModel : ViewModel() {
//
//    private val _currentStep = MutableStateFlow(0)
//    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()
//
//    private val _formData = MutableStateFlow(ProfileRequest())
//    val formData: StateFlow<ProfileRequest> = _formData.asStateFlow()
//
//    val totalSteps = 5 // 0:Welcome, 1:Personal, 2:Goals, 3:Health, 4:Diet, 5:Foods
//
//    fun nextStep() { if (_currentStep.value < totalSteps - 1) _currentStep.value++ }
//    fun previousStep() { if (_currentStep.value > 0) _currentStep.value-- }
//    fun goToStep(step: Int) { if (step in 0 until totalSteps) _currentStep.value = step }
//
//    // --- Update Functions ---
//    fun updatePersonalInfo(info: PersonalInfoReq) = _formData.update { it.copy(personalInfo = info) }
//    fun updateDailyGoals(goals: DailyGoals) = _formData.update { it.copy(dailyGoals = goals) }
//
//    private fun <T> toggleListItem(list: List<T>, item: T): List<T> {
//        return if (list.contains(item)) list.filter { it != item } else list + item
//    }
//
//    fun toggleAllergy(item: String) = _formData.update { it.copy(allergies = toggleListItem(it.allergies, item)) }
//    fun addCustomAllergy(item: String) = _formData.update { it.copy(customAllergies = it.customAllergies + item.trim()) }
//
//    fun toggleHealthCondition(item: String) = _formData.update { it.copy(healthConditions = toggleListItem(it.healthConditions, item)) }
//    fun addCustomHealthCondition(item: String) = _formData.update { it.copy(customHealthConditions = it.customHealthConditions + item.trim()) }
//
//    fun toggleDietaryRestriction(item: String) = _formData.update { it.copy(dietaryRestrictions = toggleListItem(it.dietaryRestrictions, item)) }
//
//    fun toggleLikedFood(item: String) = _formData.update {
//        // Ensure an item is not in both liked and disliked lists
//        val newDisliked = it.dislikedFoods.filter { food -> food != item }
//        it.copy(likedFoods = toggleListItem(it.likedFoods, item), dislikedFoods = newDisliked)
//    }
//
//    fun toggleDislikedFood(item: String) = _formData.update {
//        val newLiked = it.likedFoods.filter { food -> food != item }
//        it.copy(dislikedFoods = toggleListItem(it.dislikedFoods, item), likedFoods = newLiked)
//    }
//}

class OnboardingViewModel : ViewModel() {

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _formData = MutableStateFlow(ProfileRequest())
    val formData: StateFlow<ProfileRequest> = _formData.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    // Tambahkan Goals ke dalam total steps
    val totalSteps = 6 // 0:Welcome, 1:Personal, 2:Goals, 3:Health, 4:Diet, 5:Foods


    fun loadProfile(existingProfile: ProfileRequest) {
        _formData.value = existingProfile
        _isEditMode.value = true
    }

    fun nextStep() {
        if (_currentStep.value == 1) { // Saat akan pindah dari Personal Info ke Goals
            calculateAndSetRecommendedGoals()
        }
        if (_currentStep.value < totalSteps - 1) _currentStep.value++
    }
    fun previousStep() { if (_currentStep.value > 0) _currentStep.value-- }
    fun goToStep(step: Int) { if (step in 0 until totalSteps) _currentStep.value = step }

    // --- Update Functions ---
    fun updatePersonalInfo(info: PersonalInfoReq) = _formData.update { it.copy(personalInfo = info) }
    fun updateDailyGoals(goals: DailyGoals) = _formData.update { it.copy(dailyGoals = goals) }

    // --- Logika Kalkulasi Goals ---
    private fun calculateAndSetRecommendedGoals() {
        val info = _formData.value.personalInfo ?: return

        // Pastikan semua data yang dibutuhkan ada
        val age = info.age ?: return
        val height = info.height ?: return
        val weight = info.weight ?: return
        val gender = info.gender ?: return
        val activityLevel = info.activityLevel ?: return

        // 1. Hitung BMR (Basal Metabolic Rate) - Rumus Harris-Benedict
        val bmr = if (gender.equals("male", ignoreCase = true)) {
            88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
        } else {
            447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age)
        }

        // 2. Tentukan Activity Multiplier
        val activityMultiplier = when (activityLevel) {
            "sedentary" -> 1.2
            "light" -> 1.375
            "moderate" -> 1.55
            "active" -> 1.725
            "very-active" -> 1.9
            else -> 1.2
        }

        // 3. Hitung TDEE (Total Daily Energy Expenditure) -> Total Kalori
        val totalCalories = (bmr * activityMultiplier).roundToInt()

        // 4. Hitung Makronutrien (contoh rasio umum: 40% Karbo, 30% Protein, 30% Lemak)
        // 1g Karbo = 4 kalori, 1g Protein = 4 kalori, 1g Lemak = 9 kalori
        val carbsGrams = ((totalCalories * 0.40) / 4).roundToInt()
        val proteinGrams = ((totalCalories * 0.30) / 4).roundToInt()
        val fatGrams = ((totalCalories * 0.30) / 9).roundToInt()

        // 5. Buat objek DailyGoals dan update state
        val recommendedGoals = DailyGoals(
            calories = totalCalories.toDouble(),
            carbs = carbsGrams.toDouble(),
            protein = proteinGrams.toDouble(),
            fat = fatGrams.toDouble(),
            fiber = 30.0, // Nilai default
            sugar = 50.0  // Nilai default
        )

        updateDailyGoals(recommendedGoals)
    }

    // --- Fungsi toggle lainnya tetap sama ---
    private fun <T> toggleListItem(list: List<T>, item: T): List<T> {
        return if (list.contains(item)) list.filter { it != item } else list + item
    }

    fun toggleAllergy(item: String) = _formData.update { it.copy(allergies = toggleListItem(it.allergies, item)) }
    fun addCustomAllergy(item: String) = _formData.update {
        val trimmedItem = item.trim()
        it.copy(
            customAllergies = it.customAllergies + trimmedItem,
            allergies = it.allergies + trimmedItem // Langsung pilih item yang baru ditambahkan
        )
    }

    fun toggleHealthCondition(item: String) = _formData.update { it.copy(healthConditions = toggleListItem(it.healthConditions, item)) }
    fun addCustomHealthCondition(item: String) = _formData.update {
        val trimmedItem = item.trim()
        it.copy(
            customHealthConditions = it.customHealthConditions + trimmedItem,
            healthConditions = it.healthConditions + trimmedItem // Langsung pilih item yang baru ditambahkan
        )
    }

    fun toggleDietaryRestriction(item: String) = _formData.update { it.copy(dietaryRestrictions = toggleListItem(it.dietaryRestrictions, item)) }
    fun addCustomDietaryRestriction(item: String) = _formData.update {
        val trimmedItem = item.trim()
        it.copy(
            customDietaryRestrictions = it.customDietaryRestrictions + trimmedItem,
            dietaryRestrictions = it.dietaryRestrictions + trimmedItem // Langsung pilih item yang baru ditambahkan
        )
    }

    fun toggleLikedFood(item: String) = _formData.update {
        // Ensure an item is not in both liked and disliked lists
        val newDisliked = it.dislikedFoods.filter { food -> food != item }
        it.copy(likedFoods = toggleListItem(it.likedFoods, item), dislikedFoods = newDisliked)
    }

    fun toggleDislikedFood(item: String) = _formData.update {
        val newLiked = it.likedFoods.filter { food -> food != item }
        it.copy(dislikedFoods = toggleListItem(it.dislikedFoods, item), likedFoods = newLiked)
    }

    fun addCustomLikedFood(item: String) = _formData.update {
        val trimmedItem = item.trim()
        // Pastikan item tidak ada di disliked foods dan langsung pilih
        val newDisliked = it.dislikedFoods.filter { food -> food != trimmedItem }
        it.copy(
            customLikedFoods = it.customLikedFoods + trimmedItem,
            likedFoods = it.likedFoods + trimmedItem, // Langsung pilih item yang baru ditambahkan
            dislikedFoods = newDisliked // Hapus dari disliked jika ada
        )
    }

    fun addCustomDislikedFood(item: String) = _formData.update {
        val trimmedItem = item.trim()
        // Pastikan item tidak ada di liked foods dan langsung pilih
        val newLiked = it.likedFoods.filter { food -> food != trimmedItem }
        it.copy(
            customDislikedFoods = it.customDislikedFoods + trimmedItem,
            dislikedFoods = it.dislikedFoods + trimmedItem, // Langsung pilih item yang baru ditambahkan
            likedFoods = newLiked // Hapus dari liked jika ada
        )
    }
}

fun UserPreferences.toProfileRequest(): ProfileRequest {
    return ProfileRequest(
        personalInfo = PersonalInfoReq(
            age = this.personalInfo.age,
            gender = this.personalInfo.gender,
            height = this.personalInfo.height,
            weight = this.personalInfo.weight,
            activityLevel = this.personalInfo.activityLevel
        ),
        dailyGoals = DailyGoals(
            calories = this.dailyGoals?.calories,
            carbs = this.dailyGoals?.carbs,
            protein = this.dailyGoals?.protein,
            fat = this.dailyGoals?.fat,
            fiber = this.dailyGoals?.fiber,
            sugar = this.dailyGoals?.sugar
        ),
        // Gabungkan predefined dan custom items ke dalam selected items
        allergies = this.allergies + (this.customAllergies ?: emptyList()),
        customAllergies = this.customAllergies ?: emptyList(),

        healthConditions = this.healthConditions + (this.customHealthConditions ?: emptyList()),
        customHealthConditions = this.customHealthConditions ?: emptyList(),

        dietaryRestrictions = this.dietaryRestrictions + (this.customDietaryRestrictions ?: emptyList()),
        customDietaryRestrictions = this.customDietaryRestrictions ?: emptyList(),

        likedFoods = this.likedFoods + (this.customLikedFoods ?: emptyList()),
        customLikedFoods = this.customLikedFoods ?: emptyList(),

        dislikedFoods = this.dislikedFoods + (this.customDislikedFoods ?: emptyList()),
        customDislikedFoods = this.customDislikedFoods ?: emptyList()
    )
}
