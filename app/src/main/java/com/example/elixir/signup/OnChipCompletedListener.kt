package com.example.elixir.signup

interface OnChipCompletedListener {
    fun onChipSelected(chips: List<String>)
    fun onChipSelectedNot()
}