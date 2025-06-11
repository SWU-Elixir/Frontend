package com.example.elixir.network

data class GetStringResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: String?
)
