package com.example.elixir.Ingredient

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class IngredientItem(
    @PrimaryKey val id: Int,
    val name: String?,
    val type: String?
)
