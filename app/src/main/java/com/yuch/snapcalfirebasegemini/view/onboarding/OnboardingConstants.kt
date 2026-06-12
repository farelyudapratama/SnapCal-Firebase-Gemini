package com.yuch.snapcalfirebasegemini.view.onboarding

import com.yuch.snapcalfirebasegemini.R

object OnboardingConstants {
    val allergyOptions = listOf(
        "milk", "eggs", "fish", "shellfish", "tree nuts", "peanuts",
        "wheat", "soybeans", "sesame"
    )

    val healthConditionOptions = listOf(
        "diabetes", "hypertension", "heart disease", "high cholesterol",
        "kidney disease", "liver disease", "thyroid disorder", "arthritis"
    )

    val dietaryOptions = listOf(
        "vegetarian", "vegan", "halal", "kosher", "gluten-free",
        "dairy-free", "low-carb", "keto", "paleo"
    )

    val commonFoods = listOf(
        "chicken", "beef", "fish", "rice", "pasta", "bread", "eggs",
        "milk", "cheese", "yogurt", "apple", "banana", "orange",
        "broccoli", "spinach", "carrot", "potato", "tomato", "onion"
    )

    data class ActivityLevelOption(val key: String, val titleRes: Int, val descriptionRes: Int)
    val activityLevels = listOf(
        ActivityLevelOption("sedentary", R.string.activity_sedentary, R.string.activity_sedentary_desc),
        ActivityLevelOption("light", R.string.activity_light, R.string.activity_light_desc),
        ActivityLevelOption("moderate", R.string.activity_moderate, R.string.activity_moderate_desc),
        ActivityLevelOption("active", R.string.activity_active, R.string.activity_active_desc),
        ActivityLevelOption("very-active", R.string.activity_very_active, R.string.activity_very_active_desc)
    )

    // Helper functions to get localized text for options
    fun getLocalizedHealthCondition(condition: String): Int = when (condition) {
        "diabetes" -> R.string.condition_diabetes
        "hypertension" -> R.string.condition_hypertension
        "heart disease" -> R.string.condition_heart_disease
        "high cholesterol" -> R.string.condition_high_cholesterol
        "kidney disease" -> R.string.condition_kidney_disease
        "liver disease" -> R.string.condition_liver_disease
        "thyroid disorder" -> R.string.condition_thyroid_disorder
        "arthritis" -> R.string.condition_arthritis
        else -> R.string.condition_diabetes // fallback
    }

    fun getLocalizedAllergy(allergy: String): Int = when (allergy) {
        "milk" -> R.string.allergy_milk
        "eggs" -> R.string.allergy_eggs
        "fish" -> R.string.allergy_fish
        "shellfish" -> R.string.allergy_shellfish
        "tree nuts" -> R.string.allergy_tree_nuts
        "peanuts" -> R.string.allergy_peanuts
        "wheat" -> R.string.allergy_wheat
        "soybeans" -> R.string.allergy_soybeans
        "sesame" -> R.string.allergy_sesame
        else -> R.string.allergy_milk // fallback
    }

    fun getLocalizedDiet(diet: String): Int = when (diet) {
        "vegetarian" -> R.string.diet_vegetarian
        "vegan" -> R.string.diet_vegan
        "halal" -> R.string.diet_halal
        "kosher" -> R.string.diet_kosher
        "gluten-free" -> R.string.diet_gluten_free
        "dairy-free" -> R.string.diet_dairy_free
        "low-carb" -> R.string.diet_low_carb
        "keto" -> R.string.diet_keto
        "paleo" -> R.string.diet_paleo
        else -> R.string.diet_vegetarian // fallback
    }

    fun getLocalizedFood(food: String): Int = when (food) {
        "chicken" -> R.string.food_chicken
        "beef" -> R.string.food_beef
        "fish" -> R.string.food_fish
        "rice" -> R.string.food_rice
        "pasta" -> R.string.food_pasta
        "bread" -> R.string.food_bread
        "eggs" -> R.string.food_eggs
        "milk" -> R.string.food_milk
        "cheese" -> R.string.food_cheese
        "yogurt" -> R.string.food_yogurt
        "apple" -> R.string.food_apple
        "banana" -> R.string.food_banana
        "orange" -> R.string.food_orange
        "broccoli" -> R.string.food_broccoli
        "spinach" -> R.string.food_spinach
        "carrot" -> R.string.food_carrot
        "potato" -> R.string.food_potato
        "tomato" -> R.string.food_tomato
        "onion" -> R.string.food_onion
        else -> R.string.food_chicken // fallback
    }
}