package com.example.elixir.challenge.network

import com.example.elixir.challenge.data.ChallengeDetailEntity
import com.google.gson.annotations.SerializedName

// 서버 응답의 기본 구조
data class ApiResponse<T>(
    val status: Int,
    val code: String,
    val message: String,
    val data: T
)

// 리스트 응답용 (여러 챌린지) - ChallengeDetailEntity 직접 사용
typealias ChallengeListResponse = ApiResponse<List<ChallengeDetailEntity>>

// 단일 챌린지 응답용 - ChallengeDetailEntity 직접 사용
typealias ChallengeSingleResponse = ApiResponse<ChallengeDetailEntity>

// Progress 응답용
typealias ChallengeProgressResponse = ApiResponse<ChallengeProgressData>

// Completion 응답용
typealias ChallengeCompletionResponse = ApiResponse<ChallengeCompletionRawData>

data class ChallengeProgressData(
    @SerializedName("challengeId") val challengeId: Int,
    @SerializedName("step1Goal1Achieved") val step1Goal1Achieved: Boolean = false,
    @SerializedName("step1Goal2Achieved") val step1Goal2Achieved: Boolean = false,
    @SerializedName("step2Goal1Active") val step2Goal1Active: Boolean = false,
    @SerializedName("step2Goal2Active") val step2Goal2Active: Boolean = false,
    @SerializedName("step2Goal1Achieved") val step2Goal1Achieved: Boolean = false,
    @SerializedName("step2Goal2Achieved") val step2Goal2Achieved: Boolean = false,
    @SerializedName("step3Goal1Active") val step3Goal1Active: Boolean = false,
    @SerializedName("step3Goal2Active") val step3Goal2Active: Boolean = false,
    @SerializedName("step3Goal1Achieved") val step3Goal1Achieved: Boolean = false,
    @SerializedName("step3Goal2Achieved") val step3Goal2Achieved: Boolean = false,
    @SerializedName("step4Goal1Active") val step4Goal1Active: Boolean = false,
    @SerializedName("step4Goal2Active") val step4Goal2Active: Boolean = false,
    @SerializedName("step4Goal1Achieved") val step4Goal1Achieved: Boolean = false,
    @SerializedName("step4Goal2Achieved") val step4Goal2Achieved: Boolean = false,
    @SerializedName("challengeCompleted") val challengeCompleted: Boolean = false,
)

data class ChallengeCompletionRawData(
    @SerializedName("challengeCompleted") val challengeCompleted: Boolean = false,
    @SerializedName("achievementName") val achievementName: String? = null,
    @SerializedName("achievementImageUrl") val achievementImageUrl: String? = null
)