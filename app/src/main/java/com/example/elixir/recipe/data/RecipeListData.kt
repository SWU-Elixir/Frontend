package com.example.elixir.recipe.data

data class RecipeListData(
    val content: List<GetRecipeData>,
    val pageable: PageableData,
    val last: Boolean,
    val totalPages: Int,
    val totalElements: Int,
    val size: Int,
    val number: Int,
    val sort: SortData,
    val numberOfElements: Int,
    val first: Boolean,
    val empty: Boolean
)
