package com.example.elixir.calendar.data

data class DietLogDto(
    val name: String,
    val type: String,
    val score: Int,
    val ingredientTagId: List<Int>,
    val time: String
)