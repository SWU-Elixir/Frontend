package com.example.elixir.challenge.network

import retrofit2.http.*

interface ChallengeApi {

    @GET("/api/challenge/year/{year}")
    suspend fun getChallengesByYear(@Path("year") year: Int): ChallengeListResponse

    @GET("/api/challenge/{challengeId}")
    suspend fun getChallengeById(@Path("challengeId") id: Int): ChallengeSingleResponse

    @GET("/api/challenge/{challengeId}/progress")
    suspend fun getChallengeProgress(@Path("challengeId") challengeId: Int): ChallengeProgressResponse

    @GET("/api/challenge/completion")
    suspend fun getChallengeCompletion(): ChallengeCompletionResponse
}