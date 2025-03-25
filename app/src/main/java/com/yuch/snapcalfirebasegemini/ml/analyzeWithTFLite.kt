//package com.yuch.snapcalfirebasegemini.ml
//
//fun analyzeWithTFLite(imagePath: String) {
//    viewModelScope.launch {
//        isLoading.value = true
//
//        try {
//            val bitmap = BitmapFactory.decodeFile(imagePath)
//            val result = tfliteClassifier.classify(bitmap) // Fungsi untuk menjalankan model
//
//            analysisResult.value = ResultState.Success(
//                AnalysisData(
//                    foodName = result.foodName,
//                    calories = result.calories,
//                    carbs = result.carbs,
//                    protein = result.protein,
//                    totalFat = result.totalFat,
//                    saturatedFat = result.saturatedFat,
//                    fiber = result.fiber,
//                    sugar = result.sugar
//                )
//            )
//        } catch (e: Exception) {
//            errorMessage.value = "Error analyzing with TFLite: ${e.message}"
//        } finally {
//            isLoading.value = false
//        }
//    }
//}
