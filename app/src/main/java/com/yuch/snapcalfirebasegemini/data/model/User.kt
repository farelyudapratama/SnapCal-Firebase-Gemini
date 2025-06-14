package com.yuch.snapcalfirebasegemini.data.model

/**
 * Data class untuk menyimpan informasi pengguna
 */
data class User(
    val uid: String,
    val name: String,
    val email: String,
    val profileImageUrl: String? = null,
    val createdAt: Long? = null
)

/**
 * Data class untuk menyimpan preferensi pengguna
 */
data class UserPreferences(
    val age: Int,
    val gender: String,
    val height: Double, // cm
    val weight: Double, // kg
    val activityLevel: String,
    val goal: String,
    val dailyCalorieTarget: Int,
    val dailyCarbsTarget: Int,
    val dailyProteinTarget: Int,
    val dailyFatTarget: Int
)

/**
 * Data class untuk permintaan pembuatan atau pembaruan profil
 */
data class ProfileRequest(
    val age: Int,
    val gender: String,
    val height: Double,
    val weight: Double,
    val activityLevel: String,
    val goal: String,
    val dailyCalorieTarget: Int? = null,
    val dailyCarbsTarget: Int? = null,
    val dailyProteinTarget: Int? = null,
    val dailyFatTarget: Int? = null
)
