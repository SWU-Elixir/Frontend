package com.example.elixir.recipe.network.response

import com.example.elixir.recipe.data.GetRecipeData
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.data.RecipeListData

data class GetRecipeListResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: RecipeListData
)
