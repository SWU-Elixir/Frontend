package com.example.elixir.signup

interface OnProfileCompletedListener {
    fun onProfileCompleted(img: String, nick: String, sex: String, birthYear: Int)
    fun onProfileInvalid()
}