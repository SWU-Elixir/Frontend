package com.example.elixir

data class SignupData(
    var id: String = "",
    var pw: String = "",
    var nickname: String = "",
    var profileImage: String = "",     // 예: Base64 또는 이미지 URL
    var sex: String = "",
    var birthYear: Int = 0,
    var allergies: List<String> = listOf(),
    var preferredDiets: List<String> = listOf(),
    var preferredRecipes: List<String> = listOf(),
    var signupReason: String = ""
)