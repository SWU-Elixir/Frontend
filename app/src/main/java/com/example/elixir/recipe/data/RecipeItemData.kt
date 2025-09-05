package com.example.elixir.recipe.data

data class RecipeItemData(
    var id: Int,
    var likedByCurrentUser: Boolean,
    var scrappedByCurrentUser: Boolean,
    var title: String,
    var imageUrl: String?,
    var categorySlowAging: String?,
    var categoryType: String?,
    var difficulty: String,
    var totalTimeMinutes: Int,
    var ingredientTagIds: List<Int>?,
    var likes: Int
)