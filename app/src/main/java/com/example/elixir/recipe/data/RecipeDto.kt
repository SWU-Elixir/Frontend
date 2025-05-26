package com.example.elixir.recipe.data

data class RecipeDto(
    val title: String,
    val description: String,
    val categorySlowAging: String,
    val categoryType: String,
    val difficulty: String,
    val timeHours: Int,
    val timeMinutes: Int,
    val ingredientTagIds: List<Int>,
    val ingredients: Map<String, String>,
    val seasoning: Map<String, String>,
    val stepDescriptions: List<String>,
    val tips: String,
    val allergies: List<String>
)
