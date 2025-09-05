package com.example.elixir.ingredient.data
import androidx.room.*

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<IngredientEntity>)

    @Query("SELECT * FROM ingredients")
    suspend fun getAll(): List<IngredientEntity>
}