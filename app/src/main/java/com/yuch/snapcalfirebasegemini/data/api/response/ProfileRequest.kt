package com.yuch.snapcalfirebasegemini.data.api.response

data class ProfileRequest(
    val personalInfo: PersonalInfoReq? = null,
    val dailyGoals: DailyGoals? = null,
    val allergies: List<String> = emptyList(),
    val customAllergies: List<String> = emptyList(),
    val dietaryRestrictions: List<String> = emptyList(),
    val dislikedFoods: List<String> = emptyList(),
    val likedFoods: List<String> = emptyList(),
    val healthConditions: List<String> = emptyList(),
    val customHealthConditions: List<String> = emptyList()
)

data class PersonalInfoReq(
    val age: Int? = null,
    val gender: String? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val activityLevel: String? = null

)

data class DailyGoals(
    val calories: Int? = null,
    val carbs: Int? = null,
    val protein: Int? = null,
    val fat: Int? = null,
    val fiber: Int? = null,
    val sugar: Int? = null
)
