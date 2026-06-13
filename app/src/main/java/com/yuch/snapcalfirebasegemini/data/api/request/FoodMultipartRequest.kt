package com.yuch.snapcalfirebasegemini.data.api.request

import com.google.gson.Gson
import com.yuch.snapcalfirebasegemini.data.model.EditableFoodData
import com.yuch.snapcalfirebasegemini.data.model.UpdateFoodData
import com.yuch.snapcalfirebasegemini.utils.ImageUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

data class UploadFoodParts(
    val image: MultipartBody.Part?,
    val foodName: RequestBody,
    val mealType: RequestBody,
    val weightInGrams: RequestBody,
    val nutritionData: RequestBody
)

data class UpdateFoodParts(
    val image: MultipartBody.Part?,
    val foodName: RequestBody?,
    val mealType: RequestBody?,
    val weightInGrams: RequestBody?,
    val nutritionData: RequestBody?
)

fun EditableFoodData.toUploadFoodParts(imagePath: String?): UploadFoodParts {
    val mealTypeValue = requireNotNull(mealType) { "Please select a meal type" }

    return UploadFoodParts(
        image = imagePath.toUploadImagePart(),
        foodName = foodName.toPlainTextRequestBody(),
        mealType = mealTypeValue.toPlainTextRequestBody(),
        weightInGrams = weightInGrams.toPlainTextRequestBody(),
        nutritionData = mapOf(
            "calories" to calories,
            "carbs" to carbs,
            "protein" to protein,
            "totalFat" to totalFat,
            "saturatedFat" to saturatedFat,
            "fiber" to fiber,
            "sugar" to sugar
        ).toJsonRequestBody()
    )
}

fun UpdateFoodData.toUpdateFoodParts(imagePath: String?): UpdateFoodParts = UpdateFoodParts(
    image = imagePath.toUploadImagePart(),
    foodName = foodName?.toPlainTextRequestBody(),
    mealType = mealType?.toPlainTextRequestBody(),
    weightInGrams = weightInGrams.toString().toPlainTextRequestBody(),
    nutritionData = mapOf(
        "calories" to calories,
        "carbs" to carbs,
        "protein" to protein,
        "totalFat" to totalFat,
        "saturatedFat" to saturatedFat,
        "fiber" to fiber,
        "sugar" to sugar
    ).toJsonRequestBody()
)

private fun String?.toUploadImagePart(): MultipartBody.Part? = this?.let { imagePath ->
    ImageUtils.validateImageFile(imagePath)?.let { validationError ->
        throw IllegalArgumentException(validationError)
    }
    ImageUtils.prepareImageForUpload(imagePath).first
}

private fun String.toPlainTextRequestBody(): RequestBody =
    toRequestBody("text/plain".toMediaTypeOrNull())

private fun Map<String, Any?>.toJsonRequestBody(): RequestBody =
    Gson().toJson(this).toRequestBody("application/json".toMediaTypeOrNull())
