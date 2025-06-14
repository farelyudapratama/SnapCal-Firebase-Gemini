package com.yuch.snapcalfirebasegemini.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database utama aplikasi untuk menyimpan data lokal
 */
@Database(
    entities = [FoodEntity::class], 
    version = 2, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    
    companion object {
        /**
         * Migration dari versi 1 ke versi 2 database
         * 
         * Jika versi 1 memiliki struktur yang berbeda, kita perlu mendefinisikan migrasi
         * yang sesuai. Jika tidak ada perubahan struktur tabel, migrasi ini tidak melakukan
         * apa-apa, tetapi tetap diperlukan untuk menangani perubahan versi.
         */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Pastikan semua kolom yang diperlukan sudah ada di tabel 'foods'
                try {
                    // Cek apakah tabel foods ada
                    database.execSQL("SELECT * FROM foods LIMIT 0")
                } catch (e: Exception) {
                    // Jika tabel tidak ada, buat tabel baru
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS foods (
                            id TEXT PRIMARY KEY NOT NULL,
                            userId TEXT NOT NULL,
                            foodName TEXT NOT NULL,
                            imageUrl TEXT,
                            mealType TEXT NOT NULL,
                            weightInGrams TEXT,
                            calories REAL NOT NULL,
                            carbs REAL NOT NULL,
                            protein REAL NOT NULL,
                            totalFat REAL NOT NULL,
                            saturatedFat REAL NOT NULL,
                            fiber REAL NOT NULL,
                            sugar REAL NOT NULL,
                            createdAt INTEGER NOT NULL
                        )
                    """.trimIndent())
                }
            }
        }
    }
}
