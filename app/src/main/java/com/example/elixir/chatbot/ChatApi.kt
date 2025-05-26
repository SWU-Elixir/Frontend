package com.example.elixir.chatbot

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApi {
    @POST("/api/chat")
    suspend fun sendChat(@Body request: ChatRequestDto): Response<ChatApiResponse>
}