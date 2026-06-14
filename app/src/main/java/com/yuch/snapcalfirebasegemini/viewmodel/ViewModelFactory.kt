package com.yuch.snapcalfirebasegemini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import com.yuch.snapcalfirebasegemini.data.repository.ProfileRepository
import com.yuch.snapcalfirebasegemini.domain.auth.TokenManager

class ViewModelFactory(
    private val apiRepository: ApiRepository,
    private val profileRepository: ProfileRepository,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(tokenManager) as T
            }
            modelClass.isAssignableFrom(FoodViewModel::class.java) -> {
                FoodViewModel(apiRepository) as T
            }
            modelClass.isAssignableFrom(FoodEntryViewModel::class.java) -> {
                FoodEntryViewModel(apiRepository) as T
            }
            modelClass.isAssignableFrom(FoodDetailViewModel::class.java) -> {
                FoodDetailViewModel(apiRepository) as T
            }
            modelClass.isAssignableFrom(FoodListViewModel::class.java) -> {
                FoodListViewModel(apiRepository) as T
            }
            modelClass.isAssignableFrom(NutritionSummaryViewModel::class.java) -> {
                NutritionSummaryViewModel(apiRepository) as T
            }
            modelClass.isAssignableFrom(AnnouncementViewModel::class.java) -> {
                AnnouncementViewModel(apiRepository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(profileRepository) as T
            }
            modelClass.isAssignableFrom(AiChatViewModel::class.java) -> {
                AiChatViewModel(apiRepository) as T
            }
            modelClass.isAssignableFrom(RecommendationViewModel::class.java) -> {
                RecommendationViewModel(apiRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}
