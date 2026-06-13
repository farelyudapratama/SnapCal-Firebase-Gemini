package com.yuch.snapcalfirebasegemini.data.mapper

import android.util.Log
import com.google.gson.Gson
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeByMyModelResponse
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeMyModelResponse
import com.yuch.snapcalfirebasegemini.data.api.response.AnalyzeResult
import com.yuch.snapcalfirebasegemini.data.api.response.FoodDetectionByMyModelResult

private const val TAG = "FoodAnalysisParser"

sealed interface MyModelAnalysisResult {
    data class YoloDetections(
        val detections: List<FoodDetectionByMyModelResult>,
        val message: String
    ) : MyModelAnalysisResult

    data class AiFallback(
        val result: AnalyzeResult,
        val message: String,
        val isYoloFallback: Boolean
    ) : MyModelAnalysisResult

    data class Error(val message: String) : MyModelAnalysisResult
}

fun AnalyzeMyModelResponse.toMyModelAnalysisResult(gson: Gson = Gson()): MyModelAnalysisResult {
    if (status != "success") {
        return MyModelAnalysisResult.Error(message)
    }

    val rawJson = gson.toJson(data)
    Log.d(TAG, "Raw JSON response: $rawJson")

    return when {
        message.contains("Makanan berhasil dideteksi oleh model YoLo", ignoreCase = true) -> {
            parseYoloDetections(rawJson, message, gson)
        }

        message.contains("tidak mendeteksi", ignoreCase = true) ||
            message.contains("eksternal", ignoreCase = true) ||
            message.contains("Image analyzed successfully", ignoreCase = true) -> {
            parseAiFallback(rawJson, message, isYoloFallback = true, gson)
        }

        else -> {
            parseAiFallback(rawJson, message, isYoloFallback = false, gson)
        }
    }
}

private fun parseYoloDetections(
    rawJson: String,
    message: String,
    gson: Gson
): MyModelAnalysisResult {
    return try {
        val detectionResponse = gson.fromJson(rawJson, AnalyzeByMyModelResponse::class.java)
        val detections = detectionResponse?.detections.orEmpty()

        if (detections.isNotEmpty()) {
            MyModelAnalysisResult.YoloDetections(detections, message)
        } else {
            MyModelAnalysisResult.Error("YOLO model didn't detect any food in the image")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to parse YOLO detection result", e)
        MyModelAnalysisResult.Error("Failed to parse YOLO detection result")
    }
}

private fun parseAiFallback(
    rawJson: String,
    message: String,
    isYoloFallback: Boolean,
    gson: Gson
): MyModelAnalysisResult {
    return try {
        val aiResult = gson.fromJson(rawJson, AnalyzeResult::class.java)
        MyModelAnalysisResult.AiFallback(aiResult, message, isYoloFallback)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to parse AI analysis result", e)
        MyModelAnalysisResult.Error("Failed to parse AI analysis result")
    }
}
