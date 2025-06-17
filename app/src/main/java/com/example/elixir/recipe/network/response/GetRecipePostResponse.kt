package com.example.elixir.recipe.network.response

import com.example.elixir.recipe.data.RecipeResponseData

data class GetRecipePostResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: RecipeResponseData?
)