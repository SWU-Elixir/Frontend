package com.example.elixir.challenge

import java.math.BigInteger

data class StageItem(
    val id: BigInteger,                       // 단계 번호
    val challengeId: BigInteger,
    val stepNumber: Int,                  // 유형 번호
    val stepName: String,                 // 유형 이름
    val stepType: String,              // Meal_Record, Meal_Time, Recipe_Upload, Other
    val progressDate: String,
    val isComplete: Boolean = false        // 클리어 여부 (기본값: false)
)
