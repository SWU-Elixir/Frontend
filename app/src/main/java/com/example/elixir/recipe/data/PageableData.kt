package com.example.elixir.recipe.data

data class PageableData(
    val pageNumber: Int,
    val pageSize: Int,
    val sort: SortData,
    val offset: Int,
    val paged: Boolean,
    val unpaged: Boolean
)
