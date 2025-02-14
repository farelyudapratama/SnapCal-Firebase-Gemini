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
    var mealType: String? = null
)