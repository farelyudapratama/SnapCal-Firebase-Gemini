package com.yuch.snapcalfirebasegemini.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE createdAt >= :sevenDaysAgo ORDER BY createdAt DESC")
    suspend fun getRecentFoods(sevenDaysAgo: Long): List<FoodEntity>

    @Query("SELECT * FROM foods WHERE id = :id LIMIT 1")
    suspend fun getFoodById(id: String): FoodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodEntity>)

    @Query("DELETE FROM foods WHERE createdAt < :threshold")
    suspend fun deleteOldFoods(threshold: Long)

    @Query("DELETE FROM foods WHERE id = :id")
    suspend fun deleteFoodById(id: String)

    // deleteFoodImageById
    @Query("UPDATE foods SET imageUrl = null WHERE id = :id")
    suspend fun deleteFoodImageById(id: String)
}
