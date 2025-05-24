package com.yuch.snapcalfirebasegemini.utils

import android.graphics.BitmapFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File

object ImageUtils {

    // Fungsi untuk validasi file gambar
    fun validateImageFile(imagePath: String): String? {
        val file = File(imagePath)
        if (!file.exists()) return "File not found"
        val allowedExtensions = listOf("jpg", "jpeg", "png", "gif")
        val extension = file.extension.lowercase()
        if (extension !in allowedExtensions) return "Supported formats: JPEG, PNG, GIF"
        if (file.length() > 5 * 1024 * 1024) return "Maximum file size is 5MB"
        return null
    }

    private fun compressImage(imagePath: String): ByteArray {
        val file = File(imagePath)
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val byteArrayOutputStream = ByteArrayOutputStream()
        // Mengatur kualitas kompresi, misalnya 80% kualitas JPEG
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    // Fungsi untuk menyiapkan gambar sebelum diupload
    fun prepareImageForAnalyze(imagePath: String): Pair<MultipartBody.Part, String> {
        val file = File(imagePath)
        val mimeType = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            else -> "image/*"
        }
        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
        return Pair(imagePart, mimeType)
    }

    fun prepareImageForUpload(imagePath: String): Pair<MultipartBody.Part, String> {
        val compressedImage = compressImage(imagePath) // Kompres gambar terlebih dahulu
        val file = File(imagePath) // Gunakan file asli untuk nama file, tetapi gambar sudah terkompres
        val mimeType = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            else -> "image/*"
        }
        val requestFile = compressedImage.toRequestBody(mimeType.toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
        return Pair(imagePart, mimeType)
    }

    // Fungsi untuk mempersiapkan gambar untuk update jika diperlukan
    fun prepareImageForUpdate(imagePath: String): Pair<MultipartBody.Part, String> {
        return prepareImageForUpload(imagePath) // Bisa menggunakan fungsi yang sama untuk prepare image
    }
}
