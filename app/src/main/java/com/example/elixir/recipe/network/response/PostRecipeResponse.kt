package com.example.elixir.recipe.network.response

import com.example.elixir.recipe.data.RecipeData

data class PostRecipeResponse (
    val status: Int,
    val code: String,
    val message: String,
    val data: RecipeData?
)