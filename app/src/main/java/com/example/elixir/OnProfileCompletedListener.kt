package com.example.elixir

interface OnProfileCompletedListener {
    fun onProfileCompleted(img: String, nick: String, sex: String, birthYear: Int)
    fun onProfileInvalid()
}