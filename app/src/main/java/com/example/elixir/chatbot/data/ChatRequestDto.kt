package com.example.elixir.chatbot.data

data class ChatRequestDto(
    val chatSessionId: String? = null,
    val type: String? = null,
    val targetId: Long? = null,
    val durationDays: Int? = null,
    val includeChallengeIngredients: Boolean? = null,
    val additionalConditions: String? = null,
    val message: String? = null
) {
    companion object {
        const val TYPE_DIET_FEEDBACK = "DIET_FEEDBACK"
        const val TYPE_RECIPE_FEEDBACK = "RECIPE_FEEDBACK"
        const val TYPE_RECOMMEND = "RECOMMEND"
        const val TYPE_FREETALK = "FREETALK"
    }
} 