package com.example.elixir.challenge

import retrofit2.http.GET
import retrofit2.http.Path

interface ChallengeApi {
    @GET("/api/challenge")
    suspend fun getAllChallenges(): ChallengeResponse

    @GET("/api/challenge/{id}")
    suspend fun getChallengeById(@Path("id") id: Int): ChallengeResponse

    @GET("/api/challenge/year/{year}")
    suspend fun getChallengesByYear(@Path("year") year: Int): ChallengeResponse

}