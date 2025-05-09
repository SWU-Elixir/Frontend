package com.example.elixir.recipe

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipeEntity: RecipeEntity)

    @Query("SELECT * FROM recipe_table")
    fun getAllRecipes(): LiveData<List<RecipeEntity>>
}