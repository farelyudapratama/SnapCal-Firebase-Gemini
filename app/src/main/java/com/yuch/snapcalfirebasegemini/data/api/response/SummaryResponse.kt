package com.yuch.snapcalfirebasegemini.data.api.response

import com.google.gson.annotations.SerializedName

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
