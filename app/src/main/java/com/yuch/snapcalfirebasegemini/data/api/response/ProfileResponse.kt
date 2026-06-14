package com.yuch.snapcalfirebasegemini.data.api.response

data class PersonalInfo(
    val age: Int,
    val gender: String,
    val height: Int,
    val weight: Int,
    val activityLevel: String,
)

data class UserPreferences(
    val id: String? = null,
    val userId: String? = null,
    val email: String? = null,
    val notificationPreferences: NotificationPreferences? = null,
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
    val customLikedFoods: List<String>,
    val subscription: SubscriptionInfo? = null,
    val cooldown: CooldownInfo? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class NotificationPreferences(
    val push: Boolean = true,
    val email: Boolean = true,
    val inApp: Boolean = true
)

data class SubscriptionInfo(
    val tier: String = "free",
    val status: String = "active",
    val validUntil: String? = null
)

data class CooldownInfo(
    val until: String? = null,
    val violationCount: Int = 0,
    val reason: String? = null
)
