package com.yuch.snapcalfirebasegemini.data.api.response

data class PersonalInfo(
    val age: Int,
    val gender: String,
    val height: Int,
    val weight: Int,
    val activityLevel: String,
)

data class UserPreferences(
    val personalInfo: PersonalInfo,
    val dailyGoals: NutrientGoal?,
    val allergies: List<String>,
    val customAllergies: List<String>,
    val customHealthConditions: List<String>,
    val dietaryRestrictions: List<String>,
    val customDietaryRestrictions: List<String>,
    val dislikedFoods: List<String>,
    val customDislikedFoods: List<String>,
    val healthConditions: List<String>,
    val likedFoods: List<String>,
    val customLikedFoods: List<String>
)
