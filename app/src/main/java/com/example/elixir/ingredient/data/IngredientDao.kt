package com.example.elixir.ingredient.data
import androidx.room.*

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<IngredientData>)

    @Query("SELECT * FROM ingredients")
    suspend fun getAll(): List<IngredientData>
}