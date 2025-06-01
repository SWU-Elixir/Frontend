package com.example.elixir.recipe.network.response

import com.example.elixir.recipe.data.RecipeData

data class GetRecipeResponse (
    val status: Int,
    val code: String,
    val message: String,
    val data: RecipeData?
)