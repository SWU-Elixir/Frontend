package com.example.elixir.recipe.data

data class RecipeData(
    var id: Int,
    var authorFollowByCurrentUser: Boolean,
    var authorId: Int,
    var title: String,
    var imageUrl: String,
    var description: String,
    var categorySlowAging: String,
    var categoryType: String,
    var difficulty: String,
    var timeHours: Int,
    var timeMinutes: Int,
    var ingredientTagIds: List<Int>?,
    var ingredients: List<FlavoringItem>?,
    var seasonings: List<FlavoringItem>?,
    var stepDescriptions: List<String>,
    var stepImageUrls: List<String>,
    var tips: String?,
    var likes: Int,
    var likedByCurrentUser: Boolean,
    var scrappedByCurrentUser: Boolean,
    var createdAt: String,
    var updatedAt: String,
    var allergies: List<String>?
)