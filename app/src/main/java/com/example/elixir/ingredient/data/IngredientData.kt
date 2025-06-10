package com.example.elixir.ingredient.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class IngredientData(
    @PrimaryKey val id: Int,
    val name: String,
    val category: String?,
    val type: String?
)
