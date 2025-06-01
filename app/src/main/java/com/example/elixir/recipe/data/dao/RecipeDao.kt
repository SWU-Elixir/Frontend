package com.example.elixir.recipe.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.elixir.recipe.data.entity.RecipeEntity

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Query("SELECT * FROM recipe_table WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: Int): RecipeEntity?

    @Query("SELECT * FROM recipe_table WHERE categoryType = :categoryType AND categorySlowAging = :categorySlowAging LIMIT :size OFFSET :page")
    suspend fun getRecipes(page: Int, size: Int, categoryType: String, categorySlowAging: String): List<RecipeEntity>

    @Query("SELECT * FROM recipe_table WHERE title LIKE '%' || :keyword || '%' AND categoryType = :categoryType AND categorySlowAging = :categorySlowAging LIMIT :size OFFSET :page")
    suspend fun searchRecipes(keyword: String, page: Int, size: Int, categoryType: String, categorySlowAging: String): List<RecipeEntity>

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipe_table WHERE id = :recipeId")
    suspend fun deleteRecipeById(recipeId: Int)

    @Query("DELETE FROM recipe_table")
    suspend fun deleteAllRecipes()

    @Query("SELECT * FROM recipe_table")
    suspend fun getAllRecipes(): List<RecipeEntity>

    @Query("SELECT COUNT(*) FROM recipe_table")
    suspend fun countRecipes(): Int
}
