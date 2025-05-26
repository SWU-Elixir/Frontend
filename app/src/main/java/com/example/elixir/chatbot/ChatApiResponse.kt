package com.example.elixir.chatbot

data class ChatApiResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: ChatResponseDto?
)