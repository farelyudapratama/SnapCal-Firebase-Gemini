package com.yuch.snapcalfirebasegemini.data.local

import androidx.room.TypeConverter
import java.util.*

/**
 * Type converters for Room database
 * Handles conversion between database types and application types
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
