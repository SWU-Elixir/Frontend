package com.example.elixir.recipe.data

import androidx.lifecycle.LiveData
import com.example.elixir.recipe.data.dao.RecipeDao
import com.example.elixir.recipe.data.entity.RecipeEntity

class RecipeRepository(private val recipeDao: RecipeDao) {

    suspend fun insertRecipe(recipeEntity: RecipeEntity) {
        recipeDao.insertRecipe(recipeEntity)
    }

    fun getAllRecipes(): LiveData<List<RecipeEntity>> {
        return recipeDao.getAllRecipes()
    }
}