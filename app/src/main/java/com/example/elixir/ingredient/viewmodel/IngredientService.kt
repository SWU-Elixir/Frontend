package com.example.elixir.ingredient.viewmodel

import com.example.elixir.ingredient.data.IngredientItem
import com.example.elixir.ingredient.network.IngredientRepository

class IngredientService(
    private val repository: IngredientRepository
) {
    suspend fun getIngredients(): List<IngredientItem> {
        return repository.fetchAndSaveIngredients()
    }
    suspend fun getIngredientsFromDb(): List<IngredientItem> {
        return repository.getIngredientsFromDb()
    }
}