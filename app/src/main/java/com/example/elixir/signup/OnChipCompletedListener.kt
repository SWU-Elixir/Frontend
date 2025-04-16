package com.example.elixir.signup

interface OnChipCompletedListener {
    fun onChipSelected(isValid: Boolean)
    fun onChipSelectedNot(isValid: Boolean)
}