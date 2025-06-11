package com.example.elixir.calendar.data

import org.threeten.bp.LocalDateTime

data class DietLogData(
    val id: Int, // 고유 ID 추가
    var dietImg: String,
    var time: LocalDateTime,
    var dietTitle: String,
    var dietCategory: String,
    var ingredientTags: List<Int>,
    var score: Int
)

// 식단 기록 데이터 객체화
fun DietLogData.toEntity(): DietLogEntity {
    return DietLogEntity(
        id = this.id,
        name = this.dietTitle,
        type = this.dietCategory,
        score = this.score,
        ingredientTagId = this.ingredientTags.map { it.toInt() },
        time = this.time,
        imageUrl = this.dietImg
    )
}