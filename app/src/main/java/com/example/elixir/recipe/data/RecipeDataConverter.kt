package com.example.elixir.recipe.data
import com.example.elixir.recipe.data.entity.RecipeEntity

// 레시피 등록: 레시피 상세 테이블에서 dto로 변경
fun RecipeEntity.toDto(): RecipeDto {
    return RecipeDto(
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
        tips = this.tips,
        allergies = this.allergies
    )
}

// 레시피 등록: 레시피 상세 테이블에서 레시피 데이터로
fun RecipeEntity.toData(): RecipeData = RecipeData(
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
    authorId = authorId,
    likes = likes,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun RecipeData.toEntity(): RecipeEntity = RecipeEntity(
    id = id,
    authorFollowByCurrentUser = authorFollowByCurrentUser,
    authorId = authorId,
    title = title,
    imageUrl = imageUrl,
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
    likes = likes,
    likedByCurrentUser = likedByCurrentUser,
    scrappedByCurrentUser = scrappedByCurrentUser,
    createdAt = createdAt,
    updatedAt = updatedAt,
    allergies = allergies
)