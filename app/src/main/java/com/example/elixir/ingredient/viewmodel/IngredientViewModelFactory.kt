package com.example.elixir.ingredient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class IngredientViewModelFactory(
    private val ingredientService: IngredientService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IngredientViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IngredientViewModel(ingredientService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
