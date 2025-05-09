package com.example.elixir.chatbot

sealed class ChatItem {
    data class TextMessage(val message: String, val isFromUser: Boolean) : ChatItem()
    data class ExampleList(val examples: List<String>) : ChatItem()
    data class ChatMealList(val examples: List<ChatMeal>) : ChatItem()
    data class ChatRecipeList(val examples: List<ChatRecipe>) : ChatItem()
}

data class ChatMeal(
    val iconResId: Int, // drawable 리소스 ID
    val date: String,
    val title: String,
    val subtitle: String,
    val badgeNumber: Int
)

data class ChatRecipe(
    val iconResId: Int, // drawable 리소스 ID
    val title: String,
    val subtitle: String
)