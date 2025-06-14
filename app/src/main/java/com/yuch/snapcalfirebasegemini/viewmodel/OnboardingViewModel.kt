// File: viewmodel/OnboardingViewModel.kt
package com.yuch.snapcalfirebasegemini.viewmodel

import androidx.lifecycle.ViewModel
import com.yuch.snapcalfirebasegemini.data.api.response.DailyGoals
import com.yuch.snapcalfirebasegemini.data.api.response.PersonalInfoReq
import com.yuch.snapcalfirebasegemini.data.api.response.ProfileRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OnboardingViewModel : ViewModel() {

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _formData = MutableStateFlow(ProfileRequest())
    val formData: StateFlow<ProfileRequest> = _formData.asStateFlow()

    val totalSteps = 5 // 0:Welcome, 1:Personal, 2:Health, 3:Diet, 4:Foods

    fun nextStep() { if (_currentStep.value < totalSteps - 1) _currentStep.value++ }
    fun previousStep() { if (_currentStep.value > 0) _currentStep.value-- }
    fun goToStep(step: Int) { if (step in 0 until totalSteps) _currentStep.value = step }

    // --- Update Functions ---
    fun updatePersonalInfo(info: PersonalInfoReq) = _formData.update { it.copy(personalInfo = info) }
    fun updateDailyGoals(goals: DailyGoals) = _formData.update { it.copy(dailyGoals = goals) }

    private fun <T> toggleListItem(list: List<T>, item: T): List<T> {
        return if (list.contains(item)) list.filter { it != item } else list + item
    }

    fun toggleAllergy(item: String) = _formData.update { it.copy(allergies = toggleListItem(it.allergies, item)) }
    fun addCustomAllergy(item: String) = _formData.update { it.copy(customAllergies = it.customAllergies + item.trim()) }

    fun toggleHealthCondition(item: String) = _formData.update { it.copy(healthConditions = toggleListItem(it.healthConditions, item)) }
    fun addCustomHealthCondition(item: String) = _formData.update { it.copy(customHealthConditions = it.customHealthConditions + item.trim()) }

    fun toggleDietaryRestriction(item: String) = _formData.update { it.copy(dietaryRestrictions = toggleListItem(it.dietaryRestrictions, item)) }

    fun toggleLikedFood(item: String) = _formData.update {
        // Ensure an item is not in both liked and disliked lists
        val newDisliked = it.dislikedFoods.filter { food -> food != item }
        it.copy(likedFoods = toggleListItem(it.likedFoods, item), dislikedFoods = newDisliked)
    }

    fun toggleDislikedFood(item: String) = _formData.update {
        val newLiked = it.likedFoods.filter { food -> food != item }
        it.copy(dislikedFoods = toggleListItem(it.dislikedFoods, item), likedFoods = newLiked)
    }
}

//class OnboardingViewModel : ViewModel() {
//
//    private val _currentStep = MutableStateFlow(0)
//    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()
//
//    private val _formData = MutableStateFlow(ProfileRequest())
//    val formData: StateFlow<ProfileRequest> = _formData.asStateFlow()
//
//    // Tambahkan Goals ke dalam total steps
//    val totalSteps = 6 // 0:Welcome, 1:Personal, 2:Goals, 3:Health, 4:Diet, 5:Foods
//
//    fun nextStep() {
//        if (_currentStep.value == 1) { // Saat akan pindah dari Personal Info ke Goals
//            calculateAndSetRecommendedGoals()
//        }
//        if (_currentStep.value < totalSteps - 1) _currentStep.value++
//    }
//    fun previousStep() { if (_currentStep.value > 0) _currentStep.value-- }
//    fun goToStep(step: Int) { if (step in 0 until totalSteps) _currentStep.value = step }
//
//    // --- Update Functions ---
//    fun updatePersonalInfo(info: PersonalInfoReq) = _formData.update { it.copy(personalInfo = info) }
//    fun updateDailyGoals(goals: DailyGoals) = _formData.update { it.copy(dailyGoals = goals) }
//
//    // --- Logika Kalkulasi Goals ---
//    private fun calculateAndSetRecommendedGoals() {
//        val info = _formData.value.personalInfo ?: return
//
//        // Pastikan semua data yang dibutuhkan ada
//        val age = info.age ?: return
//        val height = info.height ?: return
//        val weight = info.weight ?: return
//        val gender = info.gender ?: return
//        val activityLevel = info.activityLevel ?: return
//
//        // 1. Hitung BMR (Basal Metabolic Rate) - Rumus Harris-Benedict
//        val bmr = if (gender.equals("male", ignoreCase = true)) {
//            88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
//        } else {
//            447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age)
//        }
//
//        // 2. Tentukan Activity Multiplier
//        val activityMultiplier = when (activityLevel) {
//            "sedentary" -> 1.2
//            "light" -> 1.375
//            "moderate" -> 1.55
//            "active" -> 1.725
//            "very-active" -> 1.9
//            else -> 1.2
//        }
//
//        // 3. Hitung TDEE (Total Daily Energy Expenditure) -> Total Kalori
//        val totalCalories = (bmr * activityMultiplier).roundToInt()
//
//        // 4. Hitung Makronutrien (contoh rasio umum: 40% Karbo, 30% Protein, 30% Lemak)
//        // 1g Karbo = 4 kalori, 1g Protein = 4 kalori, 1g Lemak = 9 kalori
//        val carbsGrams = ((totalCalories * 0.40) / 4).roundToInt()
//        val proteinGrams = ((totalCalories * 0.30) / 4).roundToInt()
//        val fatGrams = ((totalCalories * 0.30) / 9).roundToInt()
//
//        // 5. Buat objek DailyGoals dan update state
//        val recommendedGoals = DailyGoals(
//            calories = totalCalories,
//            carbs = carbsGrams,
//            protein = proteinGrams,
//            fat = fatGrams,
//            fiber = 30, // Nilai default
//            sugar = 50  // Nilai default
//        )
//
//        updateDailyGoals(recommendedGoals)
//    }
//
//    // --- Fungsi toggle lainnya tetap sama ---
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
//        val newDisliked = it.dislikedFoods.filter { food -> food != item }
//        it.copy(likedFoods = toggleListItem(it.likedFoods, item), dislikedFoods = newDisliked)
//    }
//
//    fun toggleDislikedFood(item: String) = _formData.update {
//        val newLiked = it.likedFoods.filter { food -> food != item }
//        it.copy(dislikedFoods = toggleListItem(it.dislikedFoods, item), likedFoods = newLiked)
//    }
//}