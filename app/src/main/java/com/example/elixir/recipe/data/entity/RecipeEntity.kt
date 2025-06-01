package com.example.elixir.recipe.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.data.RecipeDto
import org.threeten.bp.LocalDateTime

@Entity(tableName = "recipe_table")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "email") val email: String,                                          // 작성자 이메일
    @ColumnInfo(name = "title") val title: String,                                          // 레시피 제목
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "categorySlowAging") val categorySlowAging: String,
    @ColumnInfo(name = "categoryType") val categoryType: String,
    @ColumnInfo(name = "difficulty") val difficulty: String,
    @ColumnInfo(name = "timeHours") val timeHours: Int,
    @ColumnInfo(name = "timeMinutes") val timeMinutes: Int,
    @ColumnInfo(name = "ingredientTagIds") val ingredientTagIds: List<Int>,                 // 태그 목록
    @ColumnInfo(name = "ingredients") val ingredients: Map<String, String>,                 // 재료와 양념
    @ColumnInfo(name = "seasoning") val seasoning: Map<String, String>,
    @ColumnInfo(name = "stepDescriptions") val stepDescriptions: List<String>,              // 조리 단계 설명
    @ColumnInfo(name = "stepImageUrls") val stepImageUrls: List<String>,                    // 조리 단계 이미지 URL (nullable)
    @ColumnInfo(name = "tips") val tips: String,
    @ColumnInfo(name = "allergies") val allergies: List<String>,                            // 알레르기 정보
    @ColumnInfo(name = "imageUrl") val imageUrl: String,
    @ColumnInfo(name = "authorFollowByCurrentUser") val authorFollowByCurrentUser: Boolean, // 현재 사용자가 작성자를 팔로우하고 있는지 여부
    @ColumnInfo(name = "likedByCurrentUser") val likedByCurrentUser: Boolean,               // 현재 사용자가 좋아요를 눌렀는지 여부
    @ColumnInfo(name = "scrappedByCurrentUser") val scrappedByCurrentUser: Boolean,         // 현재 사용자가 스크랩을 눌렀는지 여부
    @ColumnInfo(name = "authorNickname") val authorNickname: String,                        // 작성자 닉네임
    @ColumnInfo(name = "authorTitle") val authorTitle: String,                              // 작성자 직책
    @ColumnInfo(name = "likes") val likes: Int = 0,                                         // 좋아요 수
    @ColumnInfo(name = "scraps") val scraps: Int = 0,                                       // 스크랩 수
    @ColumnInfo(name = "createdAt") val createdAt: LocalDateTime,
    @ColumnInfo(name = "updatedAt") val updatedAt: LocalDateTime
)
// 서버에서 받은 ID가 null인 경우는 로컬에서만 사용되는 레시피임을 나타냄
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
        seasoning = this.seasoning,
        stepDescriptions = this.stepDescriptions,
        tips = this.tips,
        allergies = this.allergies
    )
}

fun RecipeEntity.toData(): RecipeData = RecipeData(
    email = email,
    title = title,
    description = description,
    categorySlowAging = categorySlowAging,
    categoryType = categoryType,
    difficulty = difficulty,
    timeHours = timeHours,
    timeMinutes = timeMinutes,
    ingredientTagIds = ingredientTagIds,
    ingredients = ingredients,
    seasoning = seasoning,
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
    likes = likes,
    scraps = scraps,
    createdAt = createdAt,
    updatedAt = updatedAt
)
