package com.yuch.snapcalfirebasegemini.utils

import com.yuch.snapcalfirebasegemini.data.api.response.PersonalInfoReq

// Fungsi helper untuk kalkulasi TDEE
fun calculateRecommendedCalories(info: PersonalInfoReq?): Int? {
    if (info?.age == null || info.height == null || info.weight == null || info.gender == null || info.activityLevel == null) {
        return null // Tidak bisa menghitung jika data tidak lengkap
    }

    // Formula Harris-Benedict untuk BMR
    val bmr = if (info.gender.lowercase() == "male") {
        88.362 + (13.397 * info.weight) + (4.799 * info.height) - (5.677 * info.age)
    } else { // female
        447.593 + (9.247 * info.weight) + (3.098 * info.height) - (4.330 * info.age)
    }

    // Multiplier TDEE berdasarkan tingkat aktivitas
    val activityMultiplier = when (info.activityLevel) {
        "sedentary" -> 1.2
        "light" -> 1.375
        "moderate" -> 1.55
        "active" -> 1.725
        "very-active" -> 1.9
        else -> 1.2
    }

    // TDEE = BMR * Activity Multiplier
    return (bmr * activityMultiplier).toInt()
}