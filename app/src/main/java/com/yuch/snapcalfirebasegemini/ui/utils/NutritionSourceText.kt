package com.yuch.snapcalfirebasegemini.ui.utils

import java.util.Locale

fun String?.toReadableNutritionSource(): String = when (this) {
    "ai_estimate" -> "AI estimate"
    "reference_database" -> "Reference database"
    "manual" -> "Manual input"
    "cached" -> "Cached data"
    null, "" -> "Manual input"
    else -> replace('_', ' ').replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
}
