package com.yuch.snapcalfirebasegemini.data.api.response

import com.google.gson.annotations.SerializedName

data class RecommendationData(
    val userId: String = "",
    val mealType: String = "",
    val date: String = "",
    val recommendations: List<FoodRecommendation>,
    val cached: Boolean = false,
    val modelUsed: String = "",
    val generatedAt: String = "",
    val metadata: RecommendationMetadata? = null
)

data class FoodRecommendation(
    @SerializedName("_id")
    val id: String? = null,
    val foodName: String,
    val description: String,
    val calories: Int,
    val macros: Macros,
    val reasoning: String
)

data class Macros(
    val carbs: Int,
    val protein: Int,
    val fat: Int,
    val saturatedFat: Int,
    val fiber: Int,
    val sugar: Int
)

data class RecommendationMetadata(
    val modelUsed: String,
    val fallbackUsed: Boolean,
    val fromCache: Boolean,
    val generatedAt: String
)
