package com.example.elixir.login

import com.google.gson.annotations.SerializedName

// 로그인
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
