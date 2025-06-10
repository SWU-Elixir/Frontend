package com.example.elixir.recipe.data

data class CommentItem(
    var commentId: Int,
    var recipeId: Int,
    var nickname: String,
    var title: String,
    var content: String,
    var createdDate: String,
    var updatedDate: String
)
