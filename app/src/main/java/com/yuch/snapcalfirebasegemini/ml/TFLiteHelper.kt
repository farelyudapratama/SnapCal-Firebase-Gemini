//package com.yuch.snapcalfirebasegemini.ml
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.util.Log
//import org.tensorflow.lite.support.image.TensorImage
//import org.tensorflow.lite.task.vision.classifier.ImageClassifier
//
//class TFLiteHelper(private val context: Context) {
//
//    private var imageClassifier: ImageClassifier? = null
//
//    init {
//        try {
//            imageClassifier = ImageClassifier.createFromFile(context, "model.tflite")
//        } catch (e: Exception) {
//            Log.e("TFLiteHelper", "Error loading model: ${e.message}")
//        }
//    }
//
//    fun classifyImage(bitmap: Bitmap): String {
//        if (imageClassifier == null) return "Model not loaded"
//
//        val image = TensorImage.fromBitmap(bitmap)
//        val results = imageClassifier?.classify(image)
//
//        return results?.maxByOrNull { it.categories.firstOrNull()?.score ?: 0f }
//            ?.categories?.firstOrNull()?.label ?: "Unknown"
//    }
//}