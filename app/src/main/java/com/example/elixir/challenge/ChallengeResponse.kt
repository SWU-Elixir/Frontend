package com.example.elixir.challenge

data class ChallengeResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<ChallengeEntity>
)

/*
data class ChallengeResponseForYear(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<ChallengeYearEntity>
)

data class ChallengeResponseForId(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<ChallengeIdEntity>
)

data class ChallengeResponseForProgress(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<ChallengeProgressEntity>
)

data class ChallengeResponseForCompletion(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<ChallengeCompletionEntity>
)
*/