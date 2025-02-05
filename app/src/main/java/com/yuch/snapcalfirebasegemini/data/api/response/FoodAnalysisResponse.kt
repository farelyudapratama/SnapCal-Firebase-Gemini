package com.yuch.snapcalfirebasegemini.data.api.response

data class FoodAnalysisResponse(
    val foodName: String,
    val calories: Double,
    val carbs: Double,
    val protein: Double,
    val totalFat: Double,
    val saturatedFat: Double,
    val fiber: Double,
    val sugar: Double
)