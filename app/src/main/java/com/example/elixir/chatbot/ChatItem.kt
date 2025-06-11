package com.example.elixir.chatbot

//import com.example.elixir.calendar.DietLogData
//import com.example.elixir.recipe.RecipeData

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
    val id: Int,
    val imageUrl: String?,
    val date: String,
    val title: String,
    val subtitle: String,
    val badgeNumber: Int,
    val ingredientTags: List<Int>
)

data class ChatRecipe(
    val id: Long,
    val iconResUrl: String?, // drawable 리소스 ID
    val title: String,
    val subtitle: String,
    val ingredientTags: List<Int>
)

