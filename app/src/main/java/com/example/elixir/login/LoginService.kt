package com.example.elixir.login

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginService {
    @Headers(
        "Content-Type: application/json"
    )
    @POST("/api/member/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}