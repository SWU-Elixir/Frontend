package com.example.elixir.recipe.data

sealed class SearchItemData {
    data object SearchTextHeader : SearchItemData()
    data object SearchSpinnerHeader : SearchItemData()
    data class SearchItem(val data: RecipeItemData) : SearchItemData()
}