package com.yuch.snapcalfirebasegemini.data.mapper

import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.api.response.NutritionData
import com.yuch.snapcalfirebasegemini.data.local.FoodEntity
import com.yuch.snapcalfirebasegemini.utils.formatDateFromLong
import com.yuch.snapcalfirebasegemini.utils.parseCreatedAt

fun FoodEntity.toFoodItem(): FoodItem = FoodItem(
    id = id,
    userId = userId,
    foodName = foodName,
    mealType = mealType,
    weightInGrams = weightInGrams.orEmpty(),
    nutritionData = NutritionData(
        calories = calories,
        carbs = carbs,
        protein = protein,
        totalFat = totalFat,
        saturatedFat = saturatedFat,
        fiber = fiber,
        sugar = sugar
    ),
    imageUrl = imageUrl,
    createdAt = formatDateFromLong(createdAt)
)

fun FoodItem.toEntity(): FoodEntity = FoodEntity(
    id = id,
    userId = userId,
    foodName = foodName,
    imageUrl = imageUrl,
    mealType = mealType,
    weightInGrams = weightInGrams,
    calories = nutritionData.calories,
    carbs = nutritionData.carbs,
    protein = nutritionData.protein,
    totalFat = nutritionData.totalFat,
    saturatedFat = nutritionData.saturatedFat,
    fiber = nutritionData.fiber,
    sugar = nutritionData.sugar,
    createdAt = parseCreatedAt(createdAt)
)
