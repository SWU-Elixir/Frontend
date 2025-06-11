package com.example.elixir.recipe.network.response

import com.example.elixir.recipe.data.CommentItem

data class GetCommentResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: CommentItem?
)