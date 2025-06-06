package com.example.elixir.ingredient.network

import com.example.elixir.ingredient.data.IngredientData

data class IngredientResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<IngredientData>
) 