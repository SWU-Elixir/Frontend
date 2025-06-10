package com.example.elixir.recipe.network.response

data class GetCommentDeleteResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: String?
)
