package com.example.elixir.calendar.network

import com.example.elixir.calendar.network.response.GetMealResponse
import com.example.elixir.calendar.network.response.GetMealListResponse
import com.example.elixir.calendar.network.response.GetScoreResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface DietApi {
    @Multipart
    @POST("/api/diet-log")
    suspend fun uploadDietLog(
        @Part("dto") dto: RequestBody,
        @Part image: MultipartBody.Part
    ) : Response<GetMealResponse>

    @Multipart
    @PATCH("/api/diet-log")
    suspend fun updateDietLog(
        @Part("dto") dto: RequestBody,
        @Part image: MultipartBody.Part?
    ) : Response<GetMealResponse>

    @GET("/api/diet-log/{dietLogId}")
    suspend fun getDietLogById(
        @Path("dietLogId") dietLogId: Int
    ): Response<GetMealResponse>

    @GET("/api/diet-log/monthly-score/{year}/{month}")
    suspend fun getMonthlyScore(
        @Path("year") year: Int,
        @Path("month") month: Int
    ): Response<GetScoreResponse>

    @GET("/api/diet-log/by-date/{date}")
    suspend fun getDietLogsByDate(
        @Path("date") date: String
    ): Response<GetMealListResponse>

    @DELETE("/api/diet-log/{dietLogId}")
    suspend fun deleteDietLog(
        @Path("dietLogId") dietLogId: Int
    ): Response<GetMealResponse>
}