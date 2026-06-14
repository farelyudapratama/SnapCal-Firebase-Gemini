package com.yuch.snapcalfirebasegemini.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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

fun parseCreatedAtOrNull(createdAt: String?): Instant? {
    return try {
        createdAt?.takeIf { it.isNotBlank() }?.let(Instant::parse)
    } catch (e: Exception) {
        null
    }
}

fun createdAtLocalDateOrNull(createdAt: String?): LocalDate? {
    return parseCreatedAtOrNull(createdAt)?.atZone(ZoneId.systemDefault())?.toLocalDate()
}

fun formatCreatedAtForDisplay(createdAt: String?): String? {
    val zonedDateTime = parseCreatedAtOrNull(createdAt)?.atZone(ZoneId.systemDefault()) ?: return null
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    return "${zonedDateTime.format(dateFormatter)}, ${zonedDateTime.format(timeFormatter)}"
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
