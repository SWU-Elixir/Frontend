package com.example.elixir.challenge.network

import com.example.elixir.challenge.data.ChallengeEntity
import com.google.gson.annotations.SerializedName

// 리스트 응답용
data class ChallengeListResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<ChallengeEntity>
)

// 단일 챌린지 응답용
data class ChallengeSingleResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: ChallengeEntity
)

data class ChallengeProgress(
    val status: Int,
    val code: String,
    val message: String,
    val data: ChallengeProgressData
)

data class ChallengeProgressData(
    val challengeId: Int,
    val step1Goal1Achieved: Boolean = false,
    val step1Goal2Achieved: Boolean = false,
    val step2Goal1Active: Boolean = false,
    val step2Goal2Active: Boolean = false,
    val step2Goal1Achieved: Boolean = false,
    val step2Goal2Achieved: Boolean = false,
    val step3Goal1Active: Boolean = false,
    val step3Goal2Active: Boolean = false,
    val step3Goal1Achieved: Boolean = false,
    val step3Goal2Achieved: Boolean = false,
    val step4Goal1Active: Boolean = false,
    val step4Goal2Active: Boolean = false,
    val step4Goal1Achieved: Boolean = false,
    val step4Goal2Achieved: Boolean = false,
    val challengeCompleted: Boolean = false,
)
