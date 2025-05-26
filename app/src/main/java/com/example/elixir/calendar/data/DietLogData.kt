package com.example.elixir.calendar.data

import org.threeten.bp.LocalDateTime

data class DietLogData(
    var dietImg: String,
    var time: LocalDateTime,
    var dietTitle: String,
    var dietCategory: String,
    var ingredientTags: List<Int>,
    var score: Int
)