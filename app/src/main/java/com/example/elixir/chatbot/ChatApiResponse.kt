package com.example.elixir.chatbot

import org.threeten.bp.LocalDateTime
import com.google.gson.annotations.SerializedName

data class ChatApiResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: ChatResponseDto?
)

data class RecipeListResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<RecipeData>
)

data class RecipeData(
    val recipeId: Int,
    val imageUrl: String,
    val title: String,
    @SerializedName("ingredientTags")
    val ingredientTagIds: List<Int>
)

data class DietLogListResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<DietLogData>
)

data class DietLogData(
    val id: Int,
    val memberId: Int,
    val name: String,
    val imageUrl: String,
    val type: String,
    val score: Int,
    @SerializedName("ingredientTagId")
    val ingredientTagIds: List<Int>,
    val time: LocalDateTime
)