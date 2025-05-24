package com.yuch.snapcalfirebasegemini.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

fun parseCreatedAt(createdAt: String?): Long {
    return try {
        createdAt?.let {
            Instant.parse(it).toEpochMilli()
        } ?: System.currentTimeMillis() // Jika null, pakai waktu sekarang
    } catch (e: Exception) {
        System.currentTimeMillis() // Jika parsing gagal, pakai waktu sekarang
    }
}

// Fungsi untuk mengubah Long ke String (ISO 8601)
fun formatDateFromLong(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atOffset(
            ZoneOffset.UTC)
        .format(
            DateTimeFormatter.ISO_INSTANT)
}

fun formatTimestamp(timestamp: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Sesuaikan dengan UTC dari backend

        val date = inputFormat.parse(timestamp) ?: return "Unknown"

        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // Format 24 jam
        outputFormat.format(date)
    } catch (e: Exception) {
        "Unknown"
    }
}

fun formatDateHeader(timestamp: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val date = inputFormat.parse(timestamp) ?: return "Unknown"

        val outputFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        "Unknown"
    }
}