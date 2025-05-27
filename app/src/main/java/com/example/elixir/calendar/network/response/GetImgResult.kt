package com.example.elixir.calendar.network.response

data class GetImgResult(
    val status: Int,
    val code: String,
    val message: String,
    val imageUrl: String?
)
