package com.yuch.snapcalfirebasegemini.utils

import com.yuch.snapcalfirebasegemini.data.api.response.ProfileRequest
import com.yuch.snapcalfirebasegemini.viewmodel.OnboardingViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileViewModel

/**
 * Utility class to facilitate coordination between ProfileViewModel and OnboardingViewModel
 */
object ProfileOnboardingConnector {
    
    /**
     * Transfers the onboarding data to profile and triggers save
     * 
     * @param onboardingViewModel Source of onboarding data
     * @param profileViewModel Target for saving profile data
     * @param onComplete Callback invoked when operation is complete (success or failure)
     */
    fun saveOnboardingDataToProfile(
        onboardingViewModel: OnboardingViewModel,
        profileViewModel: ProfileViewModel,
        onComplete: (Boolean) -> Unit
    ) {
        // Get the current form data from onboarding
        val profileRequest: ProfileRequest = onboardingViewModel.formData.value
        
        // Use the ProfileViewModel to save the data
        profileViewModel.saveOrUpdateProfile(profileRequest, onComplete)
    }
}
