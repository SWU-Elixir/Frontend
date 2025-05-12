package com.example.elixir.network.response

data class RequestResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<String>
)