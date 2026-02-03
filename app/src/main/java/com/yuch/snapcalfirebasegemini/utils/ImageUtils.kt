package com.yuch.snapcalfirebasegemini.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.scale

object ImageUtils {

    private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024 // 5MB in bytes
    private const val TARGET_FILE_SIZE_BYTES = 4 * 1024 * 1024 // Target 4MB untuk safety margin
    private const val TAG = "ImageUtils"

    /**
     * Validasi file gambar
     * Tidak lagi mengecek ukuran karena akan otomatis dikompres
     */
    fun validateImageFile(imagePath: String): String? {
        val file = File(imagePath)
        if (!file.exists()) return "File not found"

        val allowedExtensions = listOf("jpg", "jpeg", "png", "gif", "webp")
        val extension = file.extension.lowercase()
        if (extension !in allowedExtensions) {
            return "Supported formats: JPEG, PNG, GIF, WebP"
        }

        return null
    }

    /**
     * Kompres gambar secara agresif hingga di bawah 5MB
     * Menggunakan strategi multi-level: sample size -> quality -> scaling
     */
    fun compressImageIfNeeded(imagePath: String): String {
        val originalFile = File(imagePath)
        if (!originalFile.exists()) {
            Log.e(TAG, "File doesn't exist: $imagePath")
            return imagePath
        }

        val originalSize = originalFile.length()

        // Jika sudah di bawah target, tidak perlu kompresi
        if (originalSize <= TARGET_FILE_SIZE_BYTES) {
            Log.d(TAG, "File size OK: ${originalSize / 1024}KB")
            return imagePath
        }

        Log.d(TAG, "Compressing image: ${originalSize / (1024 * 1024)}MB")

        // Buat file output
        val compressedFileName = "${originalFile.nameWithoutExtension}_compressed.jpg" // Selalu gunakan JPEG untuk hasil terbaik
        val compressedFile = File(originalFile.parentFile, compressedFileName)

        // Jika file compressed sudah ada dan ukurannya sesuai, gunakan itu
        if (compressedFile.exists() && compressedFile.length() <= MAX_FILE_SIZE_BYTES) {
            Log.d(TAG, "Using existing compressed file: ${compressedFile.length() / 1024}KB")
            return compressedFile.absolutePath
        }

        try {
            // Step 1: Hitung sample size optimal berdasarkan ukuran file
            val inSampleSize = calculateInSampleSize(originalSize)

            // Step 2: Load bitmap dengan sample size
            val options = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565 // Hemat memory
            }

            val bitmap = BitmapFactory.decodeFile(imagePath, options)
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap")
                return imagePath
            }

            Log.d(TAG, "Bitmap loaded with sample size $inSampleSize: ${bitmap.width}x${bitmap.height}")

            // Step 3: Kompres dengan strategi adaptif
            val result = compressAdaptively(bitmap, compressedFile)

            bitmap.recycle()

            if (result) {
                val finalSize = compressedFile.length()
                Log.d(TAG, "Compression successful: ${originalSize / 1024}KB -> ${finalSize / 1024}KB (${(finalSize * 100 / originalSize)}%)")
                return compressedFile.absolutePath
            } else {
                Log.e(TAG, "Compression failed")
                return imagePath
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during compression", e)
            return imagePath
        }
    }

    /**
     * Hitung sample size optimal berdasarkan ukuran file asli
     */
    private fun calculateInSampleSize(fileSize: Long): Int {
        val fileSizeMB = fileSize / (1024 * 1024)
        return when {
            fileSizeMB > 50 -> 16
            fileSizeMB > 30 -> 8
            fileSizeMB > 20 -> 4
            fileSizeMB > 10 -> 2
            else -> 1
        }
    }

    /**
     * Kompres bitmap secara adaptif hingga mencapai target size
     * Strategi: Quality -> Scale -> Quality lagi
     */
    private fun compressAdaptively(originalBitmap: Bitmap, outputFile: File): Boolean {
        var currentBitmap = originalBitmap
        var quality = 95
        val minQuality = 50
        var scaleFactor = 1.0f
        var attempt = 0
        val maxAttempts = 15

        while (attempt < maxAttempts) {
            attempt++

            val outputStream = ByteArrayOutputStream()
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val compressedBytes = outputStream.toByteArray()
            val currentSize = compressedBytes.size

            Log.d(TAG, "Attempt $attempt: Quality=$quality, Scale=$scaleFactor, Size=${currentSize / 1024}KB")

            // Jika sudah di bawah target, save dan selesai
            if (currentSize <= TARGET_FILE_SIZE_BYTES) {
                FileOutputStream(outputFile).use { it.write(compressedBytes) }
                if (currentBitmap != originalBitmap) {
                    currentBitmap.recycle()
                }
                return true
            }

            // Strategi kompresi bertahap
            when {
                // Fase 1: Turunkan quality dulu (lebih cepat)
                quality > minQuality -> {
                    // Hitung perkiraan quality yang dibutuhkan
                    val targetQuality = ((TARGET_FILE_SIZE_BYTES.toFloat() / currentSize) * quality).toInt()
                    quality = maxOf(targetQuality - 5, minQuality) // -5 untuk safety margin
                }

                // Fase 2: Kalau quality sudah minimal, scale down
                scaleFactor > 0.5f -> {
                    // Hitung scale factor yang dibutuhkan
                    val neededScale = kotlin.math.sqrt(TARGET_FILE_SIZE_BYTES.toFloat() / currentSize)
                    scaleFactor *= maxOf(neededScale * 0.9f, 0.7f) // 0.9 untuk safety margin

                    val newWidth = (originalBitmap.width * scaleFactor).toInt()
                    val newHeight = (originalBitmap.height * scaleFactor).toInt()

                    Log.d(TAG, "Scaling to ${newWidth}x${newHeight}")

                    val scaledBitmap = originalBitmap.scale(newWidth, newHeight)

                    if (currentBitmap != originalBitmap) {
                        currentBitmap.recycle()
                    }
                    currentBitmap = scaledBitmap
                    quality = 85 // Reset quality setelah scaling
                }

                // Fase 3: Scale sudah minimal, turunkan quality lagi
                quality > 30 -> {
                    quality -= 5
                }

                // Fase 4: Terakhir, paksa scale lebih kecil lagi
                else -> {
                    scaleFactor *= 0.8f
                    val newWidth = (originalBitmap.width * scaleFactor).toInt()
                    val newHeight = (originalBitmap.height * scaleFactor).toInt()

                    if (newWidth < 100 || newHeight < 100) {
                        // Terlalu kecil, save apa adanya
                        FileOutputStream(outputFile).use { it.write(compressedBytes) }
                        if (currentBitmap != originalBitmap) {
                            currentBitmap.recycle()
                        }
                        return true
                    }

                    val scaledBitmap = originalBitmap.scale(newWidth, newHeight)

                    if (currentBitmap != originalBitmap) {
                        currentBitmap.recycle()
                    }
                    currentBitmap = scaledBitmap
                    quality = 75
                }
            }
        }

        // Jika masih gagal setelah max attempts, save dengan ukuran terakhir
        val outputStream = ByteArrayOutputStream()
        currentBitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream)
        FileOutputStream(outputFile).use { it.write(outputStream.toByteArray()) }

        if (currentBitmap != originalBitmap) {
            currentBitmap.recycle()
        }

        Log.w(TAG, "Max attempts reached, final size: ${outputFile.length() / 1024}KB")
        return outputFile.length() <= MAX_FILE_SIZE_BYTES
    }

    /**
     * Prepare image untuk analisis
     * Otomatis kompres jika perlu
     */
    fun prepareImageForAnalyze(imagePath: String): Pair<MultipartBody.Part, String> {
        val processedImagePath = compressImageIfNeeded(imagePath)
        val file = File(processedImagePath)

        val mimeType = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "image/*"
        }

        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

        return Pair(imagePart, mimeType)
    }

    /**
     * Prepare image untuk upload
     * Dengan kompresi tambahan untuk memastikan ukuran optimal
     */
    fun prepareImageForUpload(imagePath: String): Pair<MultipartBody.Part, String> {
        val processedImagePath = compressImageIfNeeded(imagePath)
        val file = File(processedImagePath)

        val mimeType = "image/jpeg" // Selalu gunakan JPEG untuk upload
        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

        return Pair(imagePart, mimeType)
    }

    /**
     * Convert image ke Base64 dengan optimasi ukuran
     */
    fun prepareImageForBase64(
        imagePath: String,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80
    ): String {
        // Kompres file dulu jika perlu
        val processedImagePath = compressImageIfNeeded(imagePath)

        // Load bitmap
        val originalBitmap = BitmapFactory.decodeFile(processedImagePath)
            ?: throw IllegalArgumentException("Failed to decode image")

        try {
            // Hitung dimensi baru dengan aspect ratio
            val (newWidth, newHeight) = calculateScaledDimensions(
                originalBitmap.width,
                originalBitmap.height,
                maxWidth,
                maxHeight
            )

            // Resize jika perlu menggunakan Bitmap.createScaledBitmap
            val resizedBitmap = if (newWidth != originalBitmap.width || newHeight != originalBitmap.height) {
                originalBitmap.scale(newWidth, newHeight)
            } else {
                originalBitmap
            }

            // Kompres ke JPEG
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val byteArray = outputStream.toByteArray()

            // Cleanup
            originalBitmap.recycle()
            if (resizedBitmap != originalBitmap) {
                resizedBitmap.recycle()
            }

            // Encode ke Base64
            return Base64.encodeToString(byteArray, Base64.NO_WRAP)

        } catch (e: Exception) {
            originalBitmap.recycle()
            throw e
        }
    }

    /**
     * Hitung dimensi baru dengan mempertahankan aspect ratio
     */
    private fun calculateScaledDimensions(
        originalWidth: Int,
        originalHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Pair<Int, Int> {
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return Pair(originalWidth, originalHeight)
        }

        val widthRatio = maxWidth.toFloat() / originalWidth
        val heightRatio = maxHeight.toFloat() / originalHeight
        val scale = minOf(widthRatio, heightRatio)

        val newWidth = (originalWidth * scale).toInt()
        val newHeight = (originalHeight * scale).toInt()

        return Pair(newWidth, newHeight)
    }
}