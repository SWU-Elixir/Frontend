package com.example.elixir.challenge

data class ChallengeResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<ChallengeEntity>
)
