package com.example.elixir.chatbot

import com.example.elixir.calendar.MealPlanData
import com.example.elixir.recipe.RecipeData

sealed class ChatItem {
    data class TextMessage(
        val message: String, 
        val isFromUser: Boolean,
        val requestDto: ChatRequestDto? = null
    ) : ChatItem()
    
    data class ExampleList(
        val examples: List<String>,
        val requestDto: ChatRequestDto? = null
    ) : ChatItem()
    
    data class ChatMealList(
        val examples: List<ChatMeal>,
        val requestDto: ChatRequestDto? = null
    ) : ChatItem()
    
    data class ChatRecipeList(
        val examples: List<ChatRecipe>,
        val requestDto: ChatRequestDto? = null
    ) : ChatItem()
}

data class ChatMeal(
    val id: Long,
    val imageUrl: Int,
    val date: String,
    val title: String,
    val subtitle: String,
    val badgeNumber: Int,
    // 실제 데이터를 위한 필드 추가
    val mealData: MealPlanData? = null  // 실제 식단 데이터
)

data class ChatRecipe(
    val id: Long,
    val iconResId: Int, // drawable 리소스 ID
    val title: String,
    val subtitle: String,
    // 실제 데이터를 위한 필드 추가
    val recipeData: RecipeData? = null  // 실제 레시피 데이터
)

