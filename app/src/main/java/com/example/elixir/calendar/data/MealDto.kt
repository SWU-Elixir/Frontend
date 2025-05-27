package com.example.elixir.calendar.data

data class MealDto(
    val id: Int,
    val memberId: Int,
    val name: String,
    val imageUrl: String,
    val type: String,
    val score: Int,
    val ingredientTagId: List<Int>,
    val time: String // ISO_LOCAL_DATE_TIME 형태
)
