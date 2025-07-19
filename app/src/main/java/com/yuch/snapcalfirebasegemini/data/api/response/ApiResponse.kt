package com.yuch.snapcalfirebasegemini.data.api.response

import com.google.gson.JsonObject
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

data class AnalyzeMyModelResponse(
	val status: String,
	val message: String,
	val data: JsonObject
)

// Response untuk analisis makanan dengan model sendiri
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
// Request untuk estimasi nutrisi berdasarkan nama makanan setelah deteksi YOLO
data class NutritionEstimateRequest(
	val foodName: String,
	val description: String? = null
)

// Akhir inisialisasi data class untuk model sendiri

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

data class DailySummaryResponse(
	val date: String,
	@SerializedName("nutritionTotals")
	val data: DailySummary?,
	val goals: Goals,
	val foods: List<FoodBrief>,
	val feedback: List<String>
)

data class Goals(
	val calories: Double,
	val carbs: Double,
	val protein: Double,
	val fat: Double
)

data class FoodBrief(
	val mealType: String,
	val calories: Double,
	val time: String
)

data class DailySummary(
	@SerializedName("_id")
	val id: String? = null,
	val totalCalories: Double,
	val totalCarbs: Double,
	val totalProtein: Double,
	val totalFat: Double,
	val totalSaturatedFat: Double,
	val totalFiber: Double,
	val totalSugar: Double
)

data class WeeklySummaryResponse(
	val weekStart: String,
	val weekEnd: String,
	val dailyGoal: NutrientGoal,
	val summaries: List<DailyNutritionSummary>
)

data class NutrientGoal(
	val calories: Double,
	val carbs: Double,
	val protein: Double,
	val fat: Double,
	val fiber: Double,
	val sugar: Double
)

data class DailyNutritionSummary(
	val date: String,
	val calories: Double,
	val carbs: Double,
	val protein: Double,
	val fat: Double,
	val fiber: Double,
	val sugar: Double
)

data class PersonalInfo(
	val age: Int,
	val gender: String,
	val height: Int,
	val weight: Int,
	val activityLevel: String,
)

data class UserPreferences(
	val personalInfo: PersonalInfo,
	val dailyGoals: NutrientGoal?,
	val allergies: List<String>,
	val customAllergies: List<String>,
	val customHealthConditions: List<String>,
	val dietaryRestrictions: List<String>,
	val customDietaryRestrictions: List<String>,
	val dislikedFoods: List<String>,
	val customDislikedFoods: List<String>,
	val healthConditions: List<String>,
	val likedFoods: List<String>,
	val customLikedFoods: List<String>
)

// Rekomendasi makanan berdasarkan preferensi pengguna
data class RecommendationData(
	val userId: String,
	val mealType: String,
	val date: String,
	val recommendations: List<FoodRecommendation>,
	val metadata: RecommendationMetadata
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
