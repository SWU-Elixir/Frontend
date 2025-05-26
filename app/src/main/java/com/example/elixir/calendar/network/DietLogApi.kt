package com.example.elixir.calendar.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

interface DietLogApi {
    @Multipart
    @POST("/api/diet-log")
    suspend fun uploadDietLog(
        @Part("dto") dto: RequestBody,
        @Part image: MultipartBody.Part
    )

    @Multipart
    @PATCH("/api/diet-log")
    suspend fun updateDietLog(
        @Part("dto") dto: RequestBody,
        @Part image: MultipartBody.Part
    )

    //@GET("/api/diet-log/${dietLogId}")
    suspend fun getDietLog(
        @Part("DietLogID") dto: RequestBody
    )
}