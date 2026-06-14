package com.yuch.snapcalfirebasegemini.data.mapper

import com.yuch.snapcalfirebasegemini.data.api.response.DailyGoals
import com.yuch.snapcalfirebasegemini.data.api.response.PersonalInfoReq
import com.yuch.snapcalfirebasegemini.data.api.response.ProfileRequest
import com.yuch.snapcalfirebasegemini.data.api.response.UserPreferences

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
        allergies = this.allergies + (this.customAllergies ?: emptyList()),
        customAllergies = this.customAllergies ?: emptyList(),
        healthConditions = this.healthConditions + (this.customHealthConditions ?: emptyList()),
        customHealthConditions = this.customHealthConditions ?: emptyList(),
        dietaryRestrictions = this.dietaryRestrictions + (this.customDietaryRestrictions ?: emptyList()),
        customDietaryRestrictions = this.customDietaryRestrictions ?: emptyList(),
        likedFoods = this.likedFoods + (this.customLikedFoods ?: emptyList()).distinct(),
        customLikedFoods = this.customLikedFoods ?: emptyList(),
        dislikedFoods = this.dislikedFoods + (this.customDislikedFoods ?: emptyList()),
        customDislikedFoods = this.customDislikedFoods ?: emptyList()
    )
}
