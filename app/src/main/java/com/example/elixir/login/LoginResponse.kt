package com.example.elixir.login

import com.google.gson.annotations.SerializedName

// 임의적인 성공 여부 메세지
data class LoginResponse(@SerializedName("status")val status: Int,
                         @SerializedName("message")val message: String,
                         @SerializedName("token") val token: String?)
