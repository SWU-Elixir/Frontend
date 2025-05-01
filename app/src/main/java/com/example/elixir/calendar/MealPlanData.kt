package com.example.elixir.calendar

import java.io.Serializable
import java.math.BigInteger

data class MealPlanData(
    val id: BigInteger,
    val memberId: BigInteger,
    val name: String,
    val imageUrl: Int? = null,
    val createdAt: String,
    val mealtimes: String, // 아침, 점심, 저녁, 간식
    val score: Int, // 1, 2, 3, 4, 5
    val mealPlanIngredients: List<String>
) : Serializable
