package com.yuch.snapcalfirebasegemini.data.model

import com.yuch.snapcalfirebasegemini.data.api.response.SourceDetails

data class EditableFoodData (
    val foodName: String = "",
    val calories: String = "",
    val carbs: String = "",
    val protein: String = "",
    val totalFat: String = "",
    val saturatedFat: String = "",
    val fiber: String = "",
    val sugar: String = "",
    val sourceType: String = "manual",
    val sourceDetails: SourceDetails? = null,
    var mealType: String? = null,
    val weightInGrams: String = "100"
)

data class UpdateFoodData (
    val foodName: String?,
    val mealType: String?,
    val weightInGrams: String?,
    val calories: Double?,
    val carbs: Double?,
    val protein: Double?,
    val totalFat: Double?,
    val saturatedFat: Double?,
    val fiber: Double?,
    val sugar: Double?,
    val sourceType: String? = null,
    val sourceDetails: SourceDetails? = null

)
