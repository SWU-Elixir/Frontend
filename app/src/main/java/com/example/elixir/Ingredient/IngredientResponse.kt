package com.example.elixir.Ingredient

data class IngredientResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<IngredientItem>
) 