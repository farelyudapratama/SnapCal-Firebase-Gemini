package com.yuch.snapcalfirebasegemini.utils

import android.graphics.BitmapFactory
import android.util.Base64
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import android.graphics.Bitmap
import androidx.core.graphics.scale

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

    private fun prepareImageForBase64(imagePath: String, maxWidth: Int = 1024, maxHeight: Int = 1024, quality: Int = 80): String {
        // Load bitmap dari file
        val originalBitmap = BitmapFactory.decodeFile(imagePath)

        // Hitung scale untuk resize dengan menjaga aspect ratio
        val width = originalBitmap.width
        val height = originalBitmap.height
        var newWidth = width
        var newHeight = height

        if (width > maxWidth || height > maxHeight) {
            val widthRatio = maxWidth.toFloat() / width
            val heightRatio = maxHeight.toFloat() / height
            val scale = minOf(widthRatio, heightRatio)
            newWidth = (width * scale).toInt()
            newHeight = (height * scale).toInt()
        }

        // Resize bitmap kalau perlu
        val resizedBitmap =
            originalBitmap.scale(
                newWidth,
                newHeight
            )

        // Compress bitmap ke JPEG ke ByteArrayOutputStream
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        // Dapatkan byte array hasil kompres
        val byteArray = outputStream.toByteArray()

        // Bersihkan bitmap asli dan resize untuk mencegah memory leak
        originalBitmap.recycle()
        resizedBitmap.recycle()

        // Encode ke base64 tanpa line wrap
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Mengkonversi gambar dari path menjadi string base64
     * Digunakan untuk API yang membutuhkan format base64 (termasuk model kustom)
     */
    fun getBase64FromImagePath(imagePath: String): String? {
        return try {
            // Gunakan metode prepareImageForBase64 yang sudah ada untuk kompresi dan konversi
            prepareImageForBase64(imagePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
