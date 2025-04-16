package com.example.elixir.login

import com.google.gson.annotations.SerializedName

// 로그인
data class LoginRequest(@SerializedName("login_id") val loginId: String,
                        @SerializedName("password") val password: String)
