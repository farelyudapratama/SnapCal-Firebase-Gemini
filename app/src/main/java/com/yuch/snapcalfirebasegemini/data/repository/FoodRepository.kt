package com.yuch.snapcalfirebasegemini.data.repository

import android.util.Log
import com.google.gson.Gson
import com.yuch.snapcalfirebasegemini.data.api.ApiService
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeByMyModelResponse
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.ApiResponse
import com.yuch.snapcalfirebasegemini.data.api.response.DailySummaryResponse
import com.yuch.snapcalfirebasegemini.data.api.response.Food
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.data.api.response.FoodPage
import com.yuch.snapcalfirebasegemini.data.api.response.ImageUploadRequest
import com.yuch.snapcalfirebasegemini.data.api.response.NutritionData
import com.yuch.snapcalfirebasegemini.data.api.response.WeeklySummaryResponse
import com.yuch.snapcalfirebasegemini.data.local.FoodDao
import com.yuch.snapcalfirebasegemini.data.local.FoodEntity
import com.yuch.snapcalfirebasegemini.data.model.UpdateFoodData
import com.yuch.snapcalfirebasegemini.utils.ImageUtils
import com.yuch.snapcalfirebasegemini.utils.parseCreatedAt
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Repository untuk menangani semua operasi terkait makanan
 */
class FoodRepository(
    private val apiService: ApiService,
    private val foodDao: FoodDao?
) {
    /**
     * Mengambil semua data makanan dengan pagination
     */
    suspend fun getAllFood(page: Int): Result<FoodPage> {
        return try {
            val response = apiService.getAllFood(page)
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Response body is null"))
                
                val foodEntities = body.data?.items?.map { food ->
                    FoodEntity(
                        id = food.id,
                        userId = food.userId,
                        foodName = food.foodName,
                        imageUrl = food.imageUrl,
                        mealType = food.mealType,
                        weightInGrams = food.weightInGrams,
                        calories = food.nutritionData.calories,
                        carbs = food.nutritionData.carbs,
                        protein = food.nutritionData.protein,
                        totalFat = food.nutritionData.totalFat,
                        saturatedFat = food.nutritionData.saturatedFat,
                        fiber = food.nutritionData.fiber,
                        sugar = food.nutritionData.sugar,
                        createdAt = parseCreatedAt(food.createdAt)
                    )
                }

                // Simpan data ke Room jika tidak null
                foodEntities?.let { foodDao?.insertFoods(it) }

                // Hapus data yang lebih lama dari 7 hari
                val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
                foodDao?.deleteOldFoods(sevenDaysAgo)

                body.data?.let { Result.success(it) } ?: Result.failure(Exception("Food data is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mengambil data makanan berdasarkan tanggal
     */
    suspend fun getFoodByDate(date: String): Result<List<FoodItem>> {
        return try {
            val response = apiService.getFoodEntries(date)
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Response body is null"))
                body.data?.let { Result.success(it) } ?: Result.failure(Exception("Food data is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mengambil data makanan berdasarkan ID (mencoba dari cache terlebih dahulu)
     */
    suspend fun getFoodById(id: String, forceRefresh: Boolean = false): Result<FoodItem> {
        return try {
            // Coba ambil dari cache dulu jika tidak force refresh
            if (!forceRefresh) {
                foodDao?.getFoodById(id)?.let { cachedFood ->
                    return Result.success(
                        FoodItem(
                            id = cachedFood.id,
                            userId = cachedFood.userId,
                            foodName = cachedFood.foodName,
                            mealType = cachedFood.mealType,
                            weightInGrams = cachedFood.weightInGrams.toString(),
                            nutritionData = NutritionData(
                                calories = cachedFood.calories,
                                carbs = cachedFood.carbs,
                                protein = cachedFood.protein,
                                totalFat = cachedFood.totalFat,
                                saturatedFat = cachedFood.saturatedFat,
                                fiber = cachedFood.fiber,
                                sugar = cachedFood.sugar
                            ),
                            imageUrl = cachedFood.imageUrl,
                            createdAt = cachedFood.createdAt.toString()
                        )
                    )
                }
            }

            // Jika tidak ada di cache atau force refresh, ambil dari API
            val response = apiService.getFoodById(id)
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Response body is null"))
                body.data?.let { 
                    // Update cache
                    val foodEntity = FoodEntity(
                        id = it.id,
                        userId = it.userId,
                        foodName = it.foodName,
                        imageUrl = it.imageUrl,
                        mealType = it.mealType,
                        weightInGrams = it.weightInGrams,
                        calories = it.nutritionData.calories,
                        carbs = it.nutritionData.carbs,
                        protein = it.nutritionData.protein,
                        totalFat = it.nutritionData.totalFat,
                        saturatedFat = it.nutritionData.saturatedFat,
                        fiber = it.nutritionData.fiber,
                        sugar = it.nutritionData.sugar,
                        createdAt = parseCreatedAt(it.createdAt)
                    )
                    foodDao?.insertFood(foodEntity)
                    
                    Result.success(it)
                } ?: Result.failure(Exception("Food data is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mengambil semua data makanan dari cache lokal
     */
    suspend fun getCachedFoods(): List<FoodEntity> {
        return foodDao?.getAllFoods() ?: emptyList()
    }

    /**
     * Menganalisis gambar makanan
     */
    suspend fun analyzeFood(imagePath: String, service: String): Result<AnalyzeResult> {
        return try {
            val validationError = ImageUtils.validateImageFile(imagePath)
            if (validationError != null) throw Exception(validationError)

            val (imagePart, _) = ImageUtils.prepareImageForAnalyze(imagePath)
            val servicePart = service.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.analyzeFood(imagePart, servicePart)
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Response body is null"))
                body.data?.let { Result.success(it) } ?: Result.failure(Exception("Analysis data is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Menganalisis gambar makanan menggunakan model kustom
     */
    suspend fun analyzeFoodByCustomModel(imagePath: String): Result<AnalyzeByMyModelResponse> {
        return try {
            val base64Image = ImageUtils.getBase64FromImagePath(imagePath) 
                ?: return Result.failure(Exception("Failed to convert image to base64"))
            
            val request = ImageUploadRequest(base64Image)
            val response = apiService.analyzeFoodByMyModel(request)
            
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Response body is null"))
                body.data?.let { Result.success(it) } ?: Result.failure(Exception("Analysis data is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mengunggah data makanan baru
     */
    suspend fun uploadFood(
        imagePath: String?,
        foodName: String,
        mealType: String,
        weightInGrams: String,
        nutritionDataJson: String
    ): Result<Food> {
        return try {
            // Persiapkan bagian-bagian request
            val imagePart = imagePath?.let { 
                val (part, _) = ImageUtils.prepareImageForUpload(it)
                part
            }
            
            val foodNamePart = foodName.toRequestBody("text/plain".toMediaTypeOrNull())
            val mealTypePart = mealType.toRequestBody("text/plain".toMediaTypeOrNull())
            val weightPart = weightInGrams.toRequestBody("text/plain".toMediaTypeOrNull())
            val nutritionDataPart = nutritionDataJson.toRequestBody("application/json".toMediaTypeOrNull())

            val response = apiService.uploadFood(
                imagePart,
                foodNamePart,
                mealTypePart,
                weightPart,
                nutritionDataPart
            )

            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Response body is null"))
                body.data?.let { Result.success(it) } ?: Result.failure(Exception("Upload data is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Memperbarui data makanan yang sudah ada
     */
    suspend fun updateFood(
        id: String,
        imagePath: String?,
        updateData: UpdateFoodData
    ): Result<FoodItem> {
        return try {
            // Prepare multipart data
            val imagePart: MultipartBody.Part? = imagePath?.let {
                val (part, _) = ImageUtils.prepareImageForUpload(it)
                part
            }
            
            // Siapkan data yang akan diupdate
            val foodNamePart = updateData.foodName?.toRequestBody("text/plain".toMediaTypeOrNull())
            val weightPart = updateData.weightInGrams?.toRequestBody("text/plain".toMediaTypeOrNull())
            val mealTypePart = updateData.mealType?.toRequestBody("text/plain".toMediaTypeOrNull())
            
            // Siapkan nutrition data sebagai JSON
            val nutritionMap = mutableMapOf<String, Any?>()
            updateData.calories?.let { nutritionMap["calories"] = it }
            updateData.carbs?.let { nutritionMap["carbs"] = it }
            updateData.protein?.let { nutritionMap["protein"] = it }
            updateData.totalFat?.let { nutritionMap["totalFat"] = it }
            updateData.saturatedFat?.let { nutritionMap["saturatedFat"] = it }
            updateData.fiber?.let { nutritionMap["fiber"] = it }
            updateData.sugar?.let { nutritionMap["sugar"] = it }
            
            val nutritionJson = if (nutritionMap.isNotEmpty()) {
                com.google.gson.Gson().toJson(nutritionMap)
                    .toRequestBody("application/json".toMediaTypeOrNull())
            } else null

            val response = apiService.updateFood(
                id,
                imagePart,
                foodNamePart,
                weightPart,
                nutritionJson,
                mealTypePart
            )

            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Response body is null"))
                body.data?.let { Result.success(it) } ?: Result.failure(Exception("Update data is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Menghapus makanan berdasarkan ID
     */
    suspend fun deleteFood(id: String): Result<FoodItem> {
        return try {
            val response = apiService.deleteFood(id)
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Response body is null"))
                
                // Hapus dari cache
                foodDao?.deleteFoodById(id)
                
                body.data?.let { Result.success(it) } ?: Result.failure(Exception("Delete data is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Menghapus gambar makanan
     */
    suspend fun deleteFoodImage(id: String): Result<FoodItem> {
        return try {
            val response = apiService.deleteFoodImage(id)
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Response body is null"))
                
                // Update the local database to reflect the image deletion
                foodDao?.deleteFoodImageById(id)
                
                body.data?.let { Result.success(it) } ?: Result.failure(Exception("Delete image data is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mendapatkan ringkasan harian untuk hari ini
     */
    suspend fun getDailySummary(): Result<DailySummaryResponse> {
        return try {
            val response = apiService.getSummaryToday()
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Response body is null"))
                body.data?.let { Result.success(it) } ?: Result.failure(Exception("Daily summary data is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mendapatkan ringkasan mingguan
     */
    suspend fun getWeeklySummary(): Result<WeeklySummaryResponse> {
        return try {
            val response = apiService.getSummaryWeekly()
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Response body is null"))
                body.data?.let { Result.success(it) } ?: Result.failure(Exception("Weekly summary data is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var instance: FoodRepository? = null

        fun getInstance(apiService: ApiService, foodDao: FoodDao?): FoodRepository =
            instance ?: synchronized(this) {
                instance ?: FoodRepository(apiService, foodDao).also { instance = it }
            }
    }
    
    /**
     * Menganalisis gambar menggunakan File, metode yang digunakan oleh ViewModel
     */
    suspend fun analyzeImage(imageFile: File): Result<AnalyzeResult> {
        return analyzeFood(imageFile.absolutePath, "gemini")
    }
    
    /**
     * Menganalisis gambar dengan TFLite (placeholder)
     * Metode ini memerlukan implementasi dari ML Kit atau TensorFlow Lite
     */
    suspend fun analyzeWithTFLite(imagePath: String, context: android.content.Context): Result<AnalyzeResult> {
        // Implementasi TFLite seharusnya dilakukan di sini
        // Placeholder untuk saat ini
        return Result.failure(Exception("TFLite analysis not implemented"))
    }
    
    /**
     * Menganalisis gambar dengan model kustom menggunakan File, metode yang digunakan oleh ViewModel
     */
    suspend fun analyzeWithCustomModel(imageFile: File): Result<AnalyzeByMyModelResponse> {
        return analyzeFoodByCustomModel(imageFile.absolutePath)
    }
    
    /**
     * Mengunggah data makanan menggunakan objek Food, metode yang digunakan oleh ViewModel
     */
    suspend fun uploadFood(food: Food, imageFile: File?): Result<Food> {
        val imagePath = imageFile?.absolutePath
        val nutritionDataJson = Gson().toJson(food.nutritionData)
        
        return uploadFood(
            imagePath = imagePath,
            foodName = food.foodName,
            mealType = food.mealType,
            weightInGrams = food.weightInGrams ?: "0",
            nutritionDataJson = nutritionDataJson
        )
    }
    
    /**
     * Memperbarui data makanan, metode yang digunakan oleh ViewModel
     */
    suspend fun updateFood(foodId: String, updateData: UpdateFoodData): Result<FoodItem> {
        return updateFood(foodId, null, updateData)
    }
}
