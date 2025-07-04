package com.example.elixir.recipe.data

sealed class RecipeListItemData {
    data object RecommendHeader : RecipeListItemData()
    data object SearchSpinnerHeader : RecipeListItemData()
    data class RecipeItem(val data: RecipeItemData) : RecipeListItemData()
}
