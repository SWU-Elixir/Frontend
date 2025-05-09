package com.example.elixir.recipe

import okhttp3.MultipartBody

data class RecipeRequest(
    val title: String,
    val description: String,
    val categorySlowAging: String,
    val categoryType: String,
    val difficulty: String,
    val timeHours: Int,
    val timeMinutes: Int,
    val ingredientTagNames: List<String>,
    val ingredients: Map<String, String>,
    val seasoning: Map<String, String>,
    val stepDescriptions: List<String>,
    val tips: String,
    val allergies: List<String>,
    val imageUrl: MultipartBody.Part, // 레시피 이미지 URL
    val stepImageUrls: List<MultipartBody.Part> // 조리 단계 이미지 URL (nullable
)
