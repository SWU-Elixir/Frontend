package com.example.elixir.recipe.data

import com.example.elixir.recipe.data.entity.RecipeEntity
import org.threeten.bp.LocalDateTime

data class RecipeDto(
    val title: String,
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
    val tips: String,
    val allergies: List<String>?
)

fun RecipeDto.toEntity(
    stepImageUrls: List<String> = emptyList(),
    imageUrl: String = "",
    authorFollowByCurrentUser: Boolean = false,
    likedByCurrentUser: Boolean = false,
    scrappedByCurrentUser: Boolean = false,
    authorNickname: String = "",
    authorTitle: String = "",
    likes: Int = 0,
    scraps: Int = 0,
    createdAt: String = LocalDateTime.now().toString(),
    updatedAt: String = LocalDateTime.now().toString()
): RecipeEntity {
    return RecipeEntity(
        // id는 autoGenerate이므로 기본값(0) 사용
        title = this.title,
        description = this.description,
        categorySlowAging = this.categorySlowAging,
        categoryType = this.categoryType,
        difficulty = this.difficulty,
        timeHours = this.timeHours,
        timeMinutes = this.timeMinutes,
        ingredientTagIds = this.ingredientTagIds,
        ingredients = this.ingredients,
        seasonings = this.seasonings,
        stepDescriptions = this.stepDescriptions,
        stepImageUrls = stepImageUrls,
        tips = this.tips,
        allergies = this.allergies,
        imageUrl = imageUrl,
        authorFollowByCurrentUser = authorFollowByCurrentUser,
        likedByCurrentUser = likedByCurrentUser,
        scrappedByCurrentUser = scrappedByCurrentUser,
        authorNickname = authorNickname,
        authorTitle = authorTitle,
        likes = likes,
        scraps = scraps,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
