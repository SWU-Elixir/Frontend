package com.example.elixir.chatbot

sealed class ChatItem {
    data class TextMessage(val message: String, val isFromUser: Boolean) : ChatItem()
    data class ExampleList(val examples: List<String>) : ChatItem()
}