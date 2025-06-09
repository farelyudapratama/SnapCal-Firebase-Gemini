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