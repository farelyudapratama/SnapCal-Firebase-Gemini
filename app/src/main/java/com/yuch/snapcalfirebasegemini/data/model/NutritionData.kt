package com.yuch.snapcalfirebasegemini.data.model

data class EditableFoodData (
    val foodName: String = "",
    val calories: String = "",
    val carbs: String = "",
    val protein: String = "",
    val totalFat: String = "",
    val saturatedFat: String = "",
    val fiber: String = "",
    val sugar: String = "",
    var mealType: String? = null,
    val weightInGrams: String = "100"
)

data class UpdateFoodData (
    val foodName: String?,
    val mealType: String?,
    val calories: Double?,
    val carbs: Double?,
    val protein: Double?,
    val totalFat: Double?,
    val saturatedFat: Double?,
    val fiber: Double?,
    val sugar: Double?

)