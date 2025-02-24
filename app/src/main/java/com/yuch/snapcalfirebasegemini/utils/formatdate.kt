package com.yuch.snapcalfirebasegemini.utils

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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