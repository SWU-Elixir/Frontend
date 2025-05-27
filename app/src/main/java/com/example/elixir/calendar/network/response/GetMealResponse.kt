package com.example.elixir.calendar.network.response

import com.example.elixir.calendar.data.MealDto

data class GetMealResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: MealDto?
)