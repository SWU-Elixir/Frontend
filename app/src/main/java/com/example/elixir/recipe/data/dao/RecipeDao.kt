package com.example.elixir.recipe.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.elixir.recipe.data.entity.RecipeEntity

@Dao
interface RecipeDao {
    // 상세 레시피 넣기
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    // 상세 레시피 불러오기
    @Query("SELECT * FROM recipe_table WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: Int): RecipeEntity?

    // 레시피 업데이트
    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    // 레시피 삭제
    @Query("DELETE FROM recipe_table WHERE id = :recipeId")
    suspend fun deleteRecipeById(recipeId: Int)

    // 레시피 전체 삭제
    @Query("DELETE FROM recipe_table")
    suspend fun deleteAllRecipes()

    // 모든 레시피 불러오기
    @Query("SELECT * FROM recipe_table")
    suspend fun getAllRecipes(): List<RecipeEntity>

    // 레시피 갯수
    @Query("SELECT COUNT(*) FROM recipe_table")
    suspend fun countRecipes(): Int

    // 좋아요 상태 업데이트
    @Query("UPDATE recipe_table SET likedByCurrentUser = :liked, likes = :likes WHERE id = :recipeId")
    suspend fun updateLikeStatus(recipeId: Int, liked: Boolean, likes: Int)

    // 스크랩 상태 업데이트
    @Query("UPDATE recipe_table SET scrappedByCurrentUser = :scrapped WHERE id = :recipeId")
    suspend fun updateScrapStatus(recipeId: Int, scrapped: Boolean)
}
