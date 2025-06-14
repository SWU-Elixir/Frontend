package com.example.elixir.recipe.network.response

import com.example.elixir.recipe.data.GetRecipeData

data class GetRecipeRecommendResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<GetRecipeData>
) 