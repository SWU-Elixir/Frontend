package com.example.elixir.calendar.data

import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

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

fun MealDto.toEntity(): DietLogEntity {
    return DietLogEntity(
        id = this.id,
        name = this.name,
        type = this.type,
        score = this.score,
        ingredientTagId = this.ingredientTagId,
        time = LocalDateTime.parse(this.time, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        imageUrl = this.imageUrl
    )
}