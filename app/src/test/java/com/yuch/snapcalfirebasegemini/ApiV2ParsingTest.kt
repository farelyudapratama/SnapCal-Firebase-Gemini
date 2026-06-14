package com.yuch.snapcalfirebasegemini

import com.google.gson.Gson
import com.yuch.snapcalfirebasegemini.data.api.response.FoodItem
import com.yuch.snapcalfirebasegemini.utils.formatCreatedAtForDisplay
import com.yuch.snapcalfirebasegemini.utils.parseCreatedAtOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiV2ParsingTest {
    private val gson = Gson()

    @Test
    fun foodItem_acceptsMongoIdAndNumericWeight() {
        val json = """
            {
              "_id": "food-1",
              "userId": "user-1",
              "foodName": "Rice Bowl",
              "mealType": "lunch",
              "weightInGrams": 125,
              "nutritionData": {
                "calories": 240,
                "carbs": 52,
                "protein": 5,
                "totalFat": 1,
                "saturatedFat": 0.2,
                "fiber": 2,
                "sugar": 0.5,
                "sourceType": "ai_estimate"
              },
              "imageUrl": null,
              "createdAt": "2026-06-14T08:00:00.000Z"
            }
        """.trimIndent()

        val foodItem = gson.fromJson(json, FoodItem::class.java)

        assertEquals("food-1", foodItem.id)
        assertEquals("125", foodItem.weightInGrams)
        assertEquals("ai_estimate", foodItem.nutritionData.sourceType)
    }

    @Test
    fun foodItem_acceptsPlainIdAlias() {
        val json = """
            {
              "id": "food-2",
              "userId": "user-1",
              "foodName": "Apple",
              "mealType": "snack",
              "weightInGrams": "80",
              "nutritionData": {
                "calories": 52,
                "carbs": 14,
                "protein": 0.3,
                "totalFat": 0.2,
                "saturatedFat": 0,
                "fiber": 2.4,
                "sugar": 10
              },
              "imageUrl": null,
              "createdAt": "2026-06-14T08:00:00.000Z"
            }
        """.trimIndent()

        val foodItem = gson.fromJson(json, FoodItem::class.java)

        assertEquals("food-2", foodItem.id)
    }

    @Test
    fun dateParsing_returnsNullForInvalidInput() {
        assertNull(parseCreatedAtOrNull("not-a-date"))
        assertNull(formatCreatedAtForDisplay("not-a-date"))
        assertTrue(formatCreatedAtForDisplay("2026-06-14T08:00:00.000Z")?.isNotBlank() == true)
    }
}
