package com.example.elixir.ingredient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.elixir.ingredient.network.IngredientRepository

class IngredientViewModelFactory(
    private val ingredientRepository: IngredientRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IngredientViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IngredientViewModel(ingredientRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}