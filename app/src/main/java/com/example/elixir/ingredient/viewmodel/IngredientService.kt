package com.example.elixir.ingredient.viewmodel

import com.example.elixir.ingredient.data.IngredientData
import com.example.elixir.ingredient.network.IngredientRepository

class IngredientService(
    private val repository: IngredientRepository
) {
    suspend fun getIngredients(): List<IngredientData> {
        return repository.fetchAndSaveIngredients()
    }
    suspend fun getIngredientsFromDb(): List<IngredientData> {
        return repository.getIngredientsFromDb()
    }
}