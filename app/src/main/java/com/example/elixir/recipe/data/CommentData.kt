package com.example.elixir.recipe.data

data class CommentData(
    val commentId: String,
    val profileImageResId: Int,
    val memberTitle: String,
    val memberNickname: String,
    val commentText: String,
    val date: String
)
