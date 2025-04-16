package com.example.elixir

interface OnChipCompletedListener {
    fun onChipSelected(isValid: Boolean)
    fun onChipSelectedNot(isValid: Boolean)
}