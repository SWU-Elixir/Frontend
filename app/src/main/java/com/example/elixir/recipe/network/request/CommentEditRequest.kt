package com.example.elixir.recipe.network.request

data class CommentEditRequest(
    var commentId: Int,
    var recipeId: Int,
    var content: String
)
