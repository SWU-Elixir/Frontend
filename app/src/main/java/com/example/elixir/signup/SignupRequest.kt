package com.example.elixir.signup

data class SignupRequest(
    val email: String,
    val password: String,
    val nickname: String,
    val gender: String,
    val birthYear: Int,
    val allergies: List<String>,
    val mealStyles: List<String>,
    val recipeStyles: List<String>,
    val reasons: List<String>
)