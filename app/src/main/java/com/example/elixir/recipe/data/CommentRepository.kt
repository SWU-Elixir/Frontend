package com.example.elixir.recipe.data

import com.example.elixir.recipe.data.dao.CommentDao
import com.example.elixir.recipe.data.entity.CommentEntity
import com.example.elixir.recipe.data.entity.toEntity
import com.example.elixir.recipe.data.entity.toRequest
import com.example.elixir.recipe.network.api.CommentApi
import com.example.elixir.network.GetStringResponse
import com.example.elixir.recipe.network.response.GetCommentResponse

class CommentRepository(private val commentApi: CommentApi, private val commentDao: CommentDao) {
    // 댓글 업로드 (Room 저장 후 서버로 전송)
    suspend fun uploadComment(recipeId: Int, content: String): Result<GetCommentResponse> {
        val comment = CommentEntity(0, recipeId, content)
        val request = comment.toRequest()

        return try {
            val response = commentApi.uploadComment(recipeId, request)
            if (response.isSuccessful) {
                val savedComment = response.body()?.toEntity() // GetCommentResponse를 CommentEntity로 변환하는 확장 함수 필요
                savedComment?.let { commentDao.insert(it) }
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 댓글 수정 (Room 업데이트 후 서버로 전송)
    suspend fun updateComment(recipeId: Int, commentId: Int, content: String): Result<GetCommentResponse> {
        val comment = CommentEntity(commentId, recipeId, content)
        val request = comment.toRequest() // 혹은 content만 있는 request로 변경 가능(API 설계에 따라)

        return try {
            val response = commentApi.updateComment(recipeId, commentId, request)
            if (response.isSuccessful) {
                val updatedComment = response.body()?.toEntity()
                updatedComment?.let { commentDao.insert(it) }
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 댓글 삭제 (Room 삭제 후 서버로 전송)
    suspend fun deleteComment(recipeId: Int, commentId: Int): Result<GetStringResponse> {
        return try {
            val response = commentApi.deleteComment(recipeId, commentId)
            if (response.isSuccessful) {
                commentDao.deleteById(commentId)
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}