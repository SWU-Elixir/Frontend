package com.example.elixir.signup

interface OnProfileCompletedListener {
    fun onProfileCompleted(profileImage: String, nickname: String, gender: String?, birthYear: Int)
    fun onProfileInvalid()
}