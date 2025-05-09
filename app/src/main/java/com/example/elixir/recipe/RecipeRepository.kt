package com.example.elixir.recipe

import androidx.lifecycle.LiveData

class RecipeRepository(private val recipeDao: RecipeDao) {

    suspend fun insertRecipe(recipeEntity: RecipeEntity) {
        recipeDao.insert(recipeEntity)
    }

    fun getAllRecipes(): LiveData<List<RecipeEntity>> {
        return recipeDao.getAllRecipes()
    }
}