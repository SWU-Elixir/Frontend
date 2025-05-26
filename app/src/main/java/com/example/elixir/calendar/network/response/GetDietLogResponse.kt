package com.example.elixir.calendar.network.response

import com.example.elixir.calendar.data.DietLogData

data class GetDietLogResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: DietLogData?
)