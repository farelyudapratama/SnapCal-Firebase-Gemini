package com.yuch.snapcalfirebasegemini.data.api.response

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
	val status: String,
	val message: String,
	val data: T? = null,
	val error: ErrorResponse ? = null
)
data class ErrorResponse(
	val code: Int,
	val description: String
)

data class FoodPage(
	val page: Int,
	val totalPages: Int,
	val totalItems: Int,
	val items: List<FoodItem>
)
data class FoodItem(
	@SerializedName("_id")
	val id: String,
	val userId: String,
	val foodName: String,
	val mealType: String,
	val weightInGrams: String,
	val nutritionData: NutritionData,
	val imageUrl: String?,
	val createdAt: String
)

data class Food(
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
	val sugar: Double
)

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

data class DailySummary(
	val totalCalories: Double,
	val totalCarbs: Double,
	val totalProtein: Double,
	val totalFat: Double,
	val totalSaturatedFat: Double,
	val totalFiber: Double,
	val totalSugar: Double
)

data class AiChatResponse(
	val aiResponse: String
)

data class AiChatMessage(
	@SerializedName("_id")
	val id: String,
	val role: String,
	val content: String,
	val timestamp: String
)

data class AiChatDelete(
	val message: String,
)

data class UsageAiChat(
	val gemini: UsageAiChatDetails,
	val groq: UsageAiChatDetails
)

data class UsageAiChatDetails(
	val dailyCount: Int,
	val lastUsed: String,
	val remainingQuota: Int
)