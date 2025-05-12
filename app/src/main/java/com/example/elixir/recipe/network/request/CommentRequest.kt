package com.example.elixir.recipe.network.request

data class CommentRequest(
    val recipeId: Int,
    val content: String
)