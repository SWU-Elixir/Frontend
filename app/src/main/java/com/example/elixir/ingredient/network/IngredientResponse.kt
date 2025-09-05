package com.example.elixir.ingredient.network

import com.example.elixir.ingredient.data.IngredientEntity

data class IngredientResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<IngredientEntity>
) 