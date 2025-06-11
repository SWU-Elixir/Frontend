package com.example.elixir.login

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginService {
    @Headers("Content-Type: application/json")
    @POST("/api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/auth/logout")
    fun logout(): Call<LogoutResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/auth/refresh")
    fun refreshToken(): Call<RefreshTokenResponse>
}