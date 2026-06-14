package com.yuch.snapcalfirebasegemini.data.api.response

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class FoodPage(
    val page: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<FoodItem>
)

data class FoodListResponse(
    val status: String,
    val message: String? = null,
    val data: List<FoodItem> = emptyList(),
    val pagination: FoodPagination? = null
)

data class FoodPagination(
    val page: Int = 1,
    val limit: Int = 10,
    val total: Int = 0,
    val totalPages: Int = 1
)

data class FoodItem(
    @SerializedName(value = "_id", alternate = ["id"])
    val id: String,
    val userId: String,
    val foodName: String,
    val mealType: String,
    val weightInGrams: String = "",
    val nutritionData: NutritionData,
    val imageUrl: String?,
    val createdAt: String
)

data class Food(
    @SerializedName(value = "_id", alternate = ["id"])
    val id: String,
    val userId: String,
    val foodName: String,
    val mealType: String,
    val weightInGrams: String,
    val nutritionData: NutritionData,
    val imageUrl: String?,
)

data class NutritionData(
    val calories: Double,
    val carbs: Double,
    val protein: Double,
    val totalFat: Double,
    val saturatedFat: Double,
    val fiber: Double,
    val sugar: Double,
    val sourceType: String? = null,
    val sourceDetails: SourceDetails? = null
)

data class AnalyzeResult(
    val foodName: String,
    val calories: Double,
    val carbs: Double,
    val protein: Double,
    val totalFat: Double,
    val saturatedFat: Double,
    val fiber: Double,
    val sugar: Double,
    val sourceType: String? = null,
    val sourceDetails: SourceDetails? = null
)

data class SourceDetails(
    val estimatedBy: String? = null,
    val basis: String? = null,
    val confidenceNote: String? = null,
    val generalReferences: List<String> = emptyList(),
    val requiresUserVerification: Boolean? = null
)

data class AnalyzeMyModelResponse(
    val status: String,
    val message: String,
    val data: JsonObject
)

data class AnalyzeByMyModelResponse(
    @SerializedName("detections")
    val detections: List<FoodDetectionByMyModelResult>?
)

data class FoodDetectionByMyModelResult(
    @SerializedName("foodName")
    val foodName: String,
    @SerializedName("confidence")
    val confidence: Float
)

data class NutritionEstimateRequest(
    val foodName: String,
    val description: String? = null
)
