package com.example.elixir.Ingredient

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