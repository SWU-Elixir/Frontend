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

    @Query("SELECT * FROM recipe_table WHERE categoryType = :categoryType AND categorySlowAging = :categorySlowAging LIMIT :size OFFSET :offset")
    suspend fun getRecipes(offset: Int, size: Int, categoryType: String, categorySlowAging: String): List<RecipeEntity>

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

    // 좋아요 상태 업데이트
    @Query("UPDATE recipe_table SET likedByCurrentUser = :liked, likes = :likes WHERE id = :recipeId")
    suspend fun updateLikeStatus(recipeId: Int, liked: Boolean, likes: Int)

    // 스크랩 상태 업데이트
    @Query("UPDATE recipe_table SET scrappedByCurrentUser = :scrapped, scraps = :scraps WHERE id = :recipeId")
    suspend fun updateScrapStatus(recipeId: Int, scrapped: Boolean, scraps: Int)
}
