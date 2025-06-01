package com.example.elixir.calendar.network.response

import com.example.elixir.calendar.data.DietLogEntity
import com.example.elixir.calendar.data.MealDto
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

data class GetMealResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: MealDto?
)

fun GetMealResponse.toEntity(): DietLogEntity? {
    val meal = this.data ?: return null
    return DietLogEntity(
        id = meal.id, // 서버 id 사용, 필요시 0으로 변경
        name = meal.name,
        type = meal.type,
        score = meal.score,
        ingredientTagId = meal.ingredientTagId,
        time = LocalDateTime.parse(meal.time, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        imageUrl = meal.imageUrl
    )
}