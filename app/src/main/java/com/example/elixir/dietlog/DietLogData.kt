package com.example.elixir.dietlog

import java.time.LocalDateTime

data class DietLogData(
    var dietImg: String,
    var time: LocalDateTime,
    var dietTitle: String,
    var dietCategory: String,
    var ingredientTags: List<String>,
    var score: Int
)