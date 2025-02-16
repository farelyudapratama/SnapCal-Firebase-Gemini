package com.yuch.snapcalfirebasegemini.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val foodName: String,
    val imageUrl: String?,
    val mealType: String,
    // Nutrisi
    val calories: Double,
    val carbs: Double,
    val protein: Double,
    val totalFat: Double,
    val saturatedFat: Double,
    val fiber: Double,
    val sugar: Double,
    // Timestamp dalam milidetik
    val createdAt: Long
)
