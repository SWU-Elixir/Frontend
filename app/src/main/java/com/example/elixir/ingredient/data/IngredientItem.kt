package com.example.elixir.ingredient.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class IngredientItem(
    @PrimaryKey val id: Int,
    val name: String?,
    val category: String?,
    val categoryGroup: String?,
    val type: String?
)
