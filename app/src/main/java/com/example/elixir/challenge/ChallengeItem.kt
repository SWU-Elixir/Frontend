package com.example.elixir.challenge

data class ChallengeItem(
    val id: Int,
    val name: String,           // 챌린지 제목

    val purpose: String,            // 챌린지 목적
    val description: String,     // 챌린지 설명
    val badgeTitle: String,
    val badgeUrl: Int,

    val periodDate: String,
    val month: Int,
    val year: Int,

    val stages: MutableList<StageItem>    // 챌린지에 속한 단계 리스트
)
