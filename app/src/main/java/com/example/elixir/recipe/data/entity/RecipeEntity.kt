package com.example.elixir.recipe.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.elixir.ListConverter
import com.example.elixir.MapConverter
import com.example.elixir.network.toMultipart
import com.example.elixir.recipe.network.request.RecipeRequest

@Entity(tableName = "recipe_table")
@TypeConverters(MapConverter::class, ListConverter::class)
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                        // 로컬에서 자동 생성되는 ID
    val title: String,
    val description: String,
    val categorySlowAging: String,
    val categoryType: String,
    val difficulty: String,
    val timeHours: Int,
    val timeMinutes: Int,
    val ingredientTagNames: List<String>,   // 태그 목록
    val ingredients: Map<String, String>,   // 재료와 양념
    val seasoning: Map<String, String>,
    val stepDescriptions: List<String>,     // 조리 단계 설명
    val stepImageUrls: List<String>,       // 조리 단계 이미지 URL (nullable)
    val tips: String,
    val allergies: List<String>,            // 알레르기 정보
    val imageUrl: String,                  // 레시피 이미지 URL
    val likes: Int = 0,                     // 좋아요 수
    val scraps: Int = 0,                    // 스크랩 수
    val createdAt: String?,
    val updatedAt: String?,
    val isSynced: Boolean = false           // 서버와 동기화 여부
)
// 서버에서 받은 ID가 null인 경우는 로컬에서만 사용되는 레시피임을 나타냄
fun RecipeEntity.toRequest(): RecipeRequest {
    return RecipeRequest(
        title = this.title,
        description = this.description,
        categorySlowAging = this.categorySlowAging,
        categoryType = this.categoryType,
        difficulty = this.difficulty,
        timeHours = this.timeHours,
        timeMinutes = this.timeMinutes,
        ingredientTagNames = this.ingredientTagNames,
        ingredients = this.ingredients,
        seasoning = this.seasoning,
        stepDescriptions = this.stepDescriptions,
        tips = this.tips,
        allergies = this.allergies,
        imageUrl = this.imageUrl.toMultipart("image"), // 메인 이미지 변환
        stepImageUrls = this.stepImageUrls.map { it.toMultipart("stepImages") } // 단계 이미지 변환
    )
}