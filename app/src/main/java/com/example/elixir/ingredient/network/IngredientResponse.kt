package com.example.elixir.ingredient.network

import com.example.elixir.ingredient.data.IngredientItem

data class IngredientResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<IngredientItem>
) 