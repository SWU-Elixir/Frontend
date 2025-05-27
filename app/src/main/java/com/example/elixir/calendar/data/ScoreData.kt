package com.example.elixir.calendar.data

import org.threeten.bp.LocalDateTime

data class ScoreData(
    val id: Int,
    val time: LocalDateTime,
    val score: Int
)
