package com.example.elixir.recipe.data

import com.example.elixir.recipe.data.entity.RecipeEntity
import org.threeten.bp.LocalDateTime

data class GetRecipeData(
    var likedByCurrentUser: Boolean,
    var scrappedByCurrentUser: Boolean,
    var id: Int,
    var title: String,
    var imageUrl: String,
    var categorySlowAging: String,
    var categoryType: String,
    var difficulty: String,
    var totalTimeMinutes: Int,
    var ingredientTagIds: List<Int>,
    var likes: Int
)

fun GetRecipeData.toEntity(): RecipeEntity = RecipeEntity(
    id = this.id, // 서버 id 사용 (autoGenerate=true지만, 서버 id와 맞추는 것이 일반적)
    title = this.title,
    description = "", // 서버 응답에 없으므로 빈 값으로 채움
    categorySlowAging = this.categorySlowAging,
    categoryType = this.categoryType,
    difficulty = this.difficulty,
    timeHours = this.totalTimeMinutes / 60,
    timeMinutes = this.totalTimeMinutes % 60,
    ingredientTagIds = this.ingredientTagIds,
    ingredients = emptyList(), // 서버 응답에 없으므로 빈 Map
    seasonings = emptyList(),   // 서버 응답에 없으므로 빈 Map
    stepDescriptions = emptyList(), // 서버 응답에 없으므로 빈 List
    stepImageUrls = emptyList(),    // 서버 응답에 없으므로 빈 List
    tips = "",
    allergies = emptyList(),
    imageUrl = this.imageUrl,
    authorFollowByCurrentUser = false, // 서버 응답에 없으므로 기본값
    likedByCurrentUser = this.likedByCurrentUser,
    scrappedByCurrentUser = this.scrappedByCurrentUser,
    authorNickname = "",
    authorTitle = "",
    likes = this.likes,
    scraps = 0,
    createdAt = LocalDateTime.now().toString(),   // 서버 응답에 없으므로 현재 시각
    updatedAt = LocalDateTime.now().toString()    // 서버 응답에 없으므로 현재 시각
)

fun GetRecipeData.toRecipeData(): RecipeData {
    // 서버에서 안 오는 값은 기본값 또는 null/empty로 채워줍니다.
    val timeHours = totalTimeMinutes / 60
    val timeMinutes = totalTimeMinutes % 60

    return RecipeData(
        id = this.id,
        title = this.title,
        description = "", // 서버에서 안 오면 빈 값
        categorySlowAging = this.categorySlowAging,
        categoryType = this.categoryType,
        difficulty = this.difficulty,
        timeHours = timeHours,
        timeMinutes = timeMinutes,
        ingredientTagIds = this.ingredientTagIds,
        ingredients = emptyList(), // 서버에서 안 오면 빈 값
        seasonings = emptyList(),
        stepDescriptions = emptyList(),
        stepImageUrls = emptyList(),
        tips = "",
        allergies = emptyList(),
        imageUrl = this.imageUrl,
        authorFollowByCurrentUser = false,
        likedByCurrentUser = this.likedByCurrentUser,
        scrappedByCurrentUser = this.scrappedByCurrentUser,
        authorNickname = null,
        authorTitle = null,
        likes = this.likes,
        scraps = 0,
        comments = null,
        createdAt = "",
        updatedAt = ""
    )
}
