package com.yuch.snapcalfirebasegemini.data.api.response

data class FoodAnalysisResponse(
    val NamaMakanan: String,
    val Kalori: Int,
    val Karbohidrat: Int,
    val Protein: Int,
    val LemakTotal: Double,
    val LemakJenuh: Double,
    val Serat: Int,
    val Gula: Double
)