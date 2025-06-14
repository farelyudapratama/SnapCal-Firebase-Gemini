package com.yuch.snapcalfirebasegemini.data.model

/**
 * Data class untuk menyimpan informasi dasar tentang makanan
 */
data class Food(
    val id: String,
    val userId: String,
    val foodName: String,
    val mealType: String,
    val weightInGrams: String,
    val nutritionData: NutritionData,
    val imageUrl: String? = null,
    val createdAt: String? = null
)

/**
 * Data class untuk menyimpan informasi nutrisi makanan
 */
data class NutritionData(
    val calories: Double,
    val carbs: Double,
    val protein: Double,
    val totalFat: Double,
    val saturatedFat: Double = 0.0,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0
)

/**
 * Data class untuk menyimpan informasi makanan yang dapat diedit
 */
data class EditableFoodData(
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

/**
 * Data class untuk permintaan pembaruan informasi makanan
 */
data class UpdateFoodData(
    val foodName: String?,
    val mealType: String?,
    val weightInGrams: String?,
    val calories: Double?,
    val carbs: Double?,
    val protein: Double?,
    val totalFat: Double?,
    val saturatedFat: Double?,
    val fiber: Double?,
    val sugar: Double?
)

/**
 * Data class untuk menyimpan hasil analisa makanan
 */
data class AnalyzeResult(
    val foodName: String,
    val calories: Double,
    val carbs: Double,
    val protein: Double,
    val totalFat: Double,
    val saturatedFat: Double,
    val fiber: Double,
    val sugar: Double
)
