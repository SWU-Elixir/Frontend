package com.example.elixir.recipe.data

import com.example.elixir.recipe.data.entity.RecipeEntity
import org.threeten.bp.LocalDateTime

data class RecipeData(
    var id: Int,
    var authorNickname: String?,
    var authorTitle: String?,
    var authorFollowByCurrentUser: Boolean,
    var title: String,
    var imageUrl: String,
    var description: String,
    var categorySlowAging: String,
    var categoryType: String,
    var difficulty: String,
    var timeHours: Int,
    var timeMinutes: Int,
    var ingredientTagIds: List<Int>,
    var ingredients: List<FlavoringItem>,
    var seasonings: List<FlavoringItem>,
    var stepDescriptions: List<String>,
    var stepImageUrls: List<String>,
    var tips: String,
    var likes: Int,
    var scraps: Int = 0,
    var likedByCurrentUser: Boolean,
    var scrappedByCurrentUser: Boolean,
    var createdAt: String,
    var updatedAt: String,
    var allergies: List<String>?,
    var comments: List<CommentItem>?
)

fun RecipeData.toEntity(): RecipeEntity {
    return RecipeEntity(
        // id는 DB에서 autoGenerate이므로 기본값(0) 사용
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
        stepImageUrls = this.stepImageUrls,
        tips = this.tips,
        allergies = this.allergies,
        imageUrl = this.imageUrl,
        authorFollowByCurrentUser = this.authorFollowByCurrentUser,
        likedByCurrentUser = this.likedByCurrentUser,
        scrappedByCurrentUser = this.scrappedByCurrentUser,
        authorNickname = this.authorNickname,
        authorTitle = this.authorTitle,
        likes = this.likes,
        scraps = this.scraps,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun RecipeData.toDto(): RecipeDto {
    return RecipeDto(
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
        tips = tips,
        allergies = allergies
    )
}