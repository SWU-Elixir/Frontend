package com.example.elixir.recipe.data

data class CommentItem(
    var commentId: Int,
    var recipeId: Int,
    var nickName: String,
    var title: String?,
    var authorProfileUrl: String?,
    var content: String,
    var createdAt: String,
    var updatedAt: String
)
