package com.yuch.snapcalfirebasegemini.data.api.response

import com.google.gson.annotations.SerializedName

data class DailySummaryResponse(
    val date: String,
    @SerializedName("nutritionTotals")
    val data: DailySummary?,
    val goals: Goals?,
    val percentage: SummaryPercentage? = null,
    val foods: List<FoodBrief> = emptyList(),
    val feedback: List<String> = emptyList()
)

data class SummaryPercentage(
    val calories: Double,
    val carbs: Double
)

data class Goals(
    val calories: Double,
    val carbs: Double,
    val protein: Double,
    val fat: Double,
    val fiber: Double? = null,
    val sugar: Double? = null
)

data class FoodBrief(
    val name: String? = null,
    val mealType: String,
    val calories: Double,
    val time: String
)

data class DailySummary(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName(value = "totalCalories", alternate = ["calories"])
    val totalCalories: Double,
    @SerializedName(value = "totalCarbs", alternate = ["carbs"])
    val totalCarbs: Double,
    @SerializedName(value = "totalProtein", alternate = ["protein"])
    val totalProtein: Double,
    val totalFat: Double,
    @SerializedName(value = "totalSaturatedFat", alternate = ["saturatedFat"])
    val totalSaturatedFat: Double,
    @SerializedName(value = "totalFiber", alternate = ["fiber"])
    val totalFiber: Double,
    @SerializedName(value = "totalSugar", alternate = ["sugar"])
    val totalSugar: Double
)

data class WeeklySummaryResponse(
    val weekStart: String,
    val weekEnd: String,
    val dailyGoal: NutrientGoal,
    val summaries: List<DailyNutritionSummary>
)

data class NutrientGoal(
    val calories: Double = 0.0,
    val carbs: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0
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
