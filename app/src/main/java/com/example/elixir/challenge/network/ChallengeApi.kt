package com.example.elixir.challenge.network

import retrofit2.http.GET
import retrofit2.http.Path

interface ChallengeApi {
    @GET("/api/challenge/{id}")
    suspend fun getChallengeById(@Path("id") id: Int): ChallengeSingleResponse

    @GET("/api/challenge/year/{year}")
    suspend fun getChallengesByYear(@Path("year") year: Int): ChallengeListResponse

    @GET("/api/challenge/{challengeId}/progress")
    suspend fun getChallengeProgress(@Path("challengeId") challengeId: Int): ChallengeProgress

    @GET("/api/challenge/completion")
    suspend fun getChallengeCompletion(): ChallengeSingleResponse
}