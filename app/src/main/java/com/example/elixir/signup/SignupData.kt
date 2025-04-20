package com.example.elixir.signup

data class SignupData(
    var id: String,
    var pw: String,
    var nickname: String,
    var profileImage: String,
    var gender: String,
    var birthYear: Int,
    var allergies: List<String>,
    var preferredDiet: String,
    var preferredRecipes: List<String>,
    var signupReason: String
)