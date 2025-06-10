package com.example.elixir.recipe.data.dao

import androidx.room.*
import com.example.elixir.recipe.data.entity.CommentEntity

@Dao
interface CommentDao {
    // 댓글 추가 (충돌 시 교체)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: CommentEntity)

    // 댓글 수정 (직접 update 쿼리로도 가능)
    @Update
    suspend fun update(comment: CommentEntity)

    // 댓글 삭제 (id 기준)
    @Delete
    suspend fun delete(comment: CommentEntity)

    // 특정 댓글 삭제 (id로만 삭제)
    @Query("DELETE FROM comment_table WHERE id = :commentId")
    suspend fun deleteById(commentId: Int)

    // 레시피ID로 댓글 리스트 조회
    @Query("SELECT * FROM comment_table WHERE recipeId = :recipeId")
    fun getCommentsByRecipeId(recipeId: Int): List<CommentEntity>

    // 모든 댓글 조회
    @Query("SELECT * FROM comment_table")
    fun getAllComments(): List<CommentEntity>
}
