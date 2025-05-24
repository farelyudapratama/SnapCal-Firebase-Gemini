package com.yuch.snapcalfirebasegemini.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FoodEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
}
