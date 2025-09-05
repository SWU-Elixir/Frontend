package com.example.elixir.recipe.data

data class RecipeDto(
    val title: String,
    val description: String,
    val categorySlowAging: String,
    val categoryType: String,
    val difficulty: String,
    val timeHours: Int,
    val timeMinutes: Int,
    val ingredientTagIds: List<Int>?,
    val ingredients: List<FlavoringItem>?,
    val seasonings: List<FlavoringItem>?,
    val stepDescriptions: List<String>,
    val tips: String?,
    val allergies: List<String>?
)
