package com.example.elixir

import java.time.LocalDateTime

data class DietLogData(
    var dietImg: String, var time: LocalDateTime, var dietTitle: String,
    var ingredientTags: List<String>, var score: Int
)