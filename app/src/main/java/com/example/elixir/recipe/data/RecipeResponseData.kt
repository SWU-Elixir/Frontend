package com.example.elixir.recipe.data

import com.example.elixir.recipe.data.entity.RecipeEntity

data class RecipeResponseData(
    val id: Int,
    val email: String,
    val title: String,
    val imageUrl: String,
    val description: String,
    val categorySlowAging: String,
    val categoryType: String,
    val difficulty: String,
    val timeHours: Int,
    val timeMinutes: Int,
    val ingredientTagIds: List<Int>,
    val ingredients: List<FlavoringItem>,
    val seasonings: List<FlavoringItem>,
    val stepDescriptions: List<String>,
    val stepImageUrls: List<String>,
    val tips: String,
    val likes: Int,
    val scraps: Int,
    val createdAt: String,
    val updatedAt: String,
    val allergies: List<String>)

fun RecipeResponseData.toRecipeData(): RecipeData  = RecipeData(
    id = id,
    title = title,
    description = description,
    categorySlowAging = categorySlowAging,
    categoryType = categoryType,
    difficulty = difficulty,
    timeHours = timeHours,
    timeMinutes = timeMinutes,
    ingredientTagIds = ingredientTagIds,
    ingredients = ingredients,
    seasonings = seasonings,
    stepDescriptions = stepDescriptions,
    stepImageUrls = stepImageUrls,
    tips = tips,
    allergies = allergies,
    imageUrl = imageUrl,
    authorFollowByCurrentUser = false,
    likedByCurrentUser = false,
    scrappedByCurrentUser = false,
    authorNickname = null,
    authorTitle = null,
    authorProfileUrl = null,
    likes = likes,
    scraps = scraps,
    comments = null,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun RecipeResponseData.toEntity(
    authorFollowByCurrentUser: Boolean = false,
    likedByCurrentUser: Boolean = false,
    scrappedByCurrentUser: Boolean = false,
    authorNickname: String? = null,
    authorTitle: String? = null,
    authorProfileUrl: String? = null
): RecipeEntity {
    return RecipeEntity(
        id = id,
        title = title,
        description = description,
        categorySlowAging = categorySlowAging,
        categoryType = categoryType,
        difficulty = difficulty,
        timeHours = timeHours,
        timeMinutes = timeMinutes,
        ingredientTagIds = ingredientTagIds,
        ingredients = ingredients,
        seasonings = seasonings,
        stepDescriptions = stepDescriptions,
        stepImageUrls = stepImageUrls,
        tips = tips,
        allergies = allergies,
        imageUrl = imageUrl,
        authorFollowByCurrentUser = authorFollowByCurrentUser,
        likedByCurrentUser = likedByCurrentUser,
        scrappedByCurrentUser = scrappedByCurrentUser,
        authorNickname = authorNickname,
        authorTitle = authorTitle,
        authorProfileUrl = authorProfileUrl,
        likes = likes,
        scraps = scraps,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
