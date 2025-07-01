package com.example.elixir.recipe.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.elixir.recipe.data.FlavoringItem

// 상세 레시피 데이터
@Entity(tableName = "recipe_table")
data class RecipeEntity(
    @PrimaryKey val id: Int,
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
    val stepImageUrls: List<String>,
    val tips: String?,
    val allergies: List<String>?,
    val imageUrl: String,
    val authorFollowByCurrentUser: Boolean,
    val likedByCurrentUser: Boolean,
    val scrappedByCurrentUser: Boolean,
    val authorId: Int,
    val likes: Int,
    val createdAt: String,
    val updatedAt: String
)