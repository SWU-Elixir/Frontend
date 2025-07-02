package com.example.elixir.login.data

// 임의적인 성공 여부 메세지
data class LoginResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: TokenData?
)

data class TokenData(
    val grantType: String,
    val accessToken: String,
    val refreshToken: String
)

// 로그아웃 응답
data class LogoutResponse(
    val status: Int,
    val code: String,
    val message: String
)

// 토큰 재발급 응답
data class RefreshTokenResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: RefreshTokenData?
)

data class RefreshTokenData(
    val accessToken: String
)
