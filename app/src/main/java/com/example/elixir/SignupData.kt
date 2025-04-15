package com.example.elixir

data class SignupData(
    var id: String?,
    var pw: String?,
    var nickname: String?,
    var profileImage: String?,
    var sex: String?,
    var birthYear: Int?,
    var allergies: List<String>?,
    var preferredDiets: List<String>?,
    var preferredRecipes: List<String>?,
    var signupReason: String?
)