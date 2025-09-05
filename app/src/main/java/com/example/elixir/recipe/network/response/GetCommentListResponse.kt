package com.example.elixir.recipe.network.response

import com.example.elixir.recipe.data.CommentItem

data class GetCommentListResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<CommentItem>?
)
