package com.example.elixir.login.network

import com.example.elixir.login.data.LoginRequest
import com.example.elixir.login.data.LoginResponse
import com.example.elixir.login.data.LogoutResponse
import com.example.elixir.login.data.RefreshTokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginApi {
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