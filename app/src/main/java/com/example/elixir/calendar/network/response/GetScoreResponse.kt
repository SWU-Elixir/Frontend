package com.example.elixir.calendar.network.response

import com.example.elixir.calendar.data.ScoreData

data class GetScoreResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<ScoreData>?
)
