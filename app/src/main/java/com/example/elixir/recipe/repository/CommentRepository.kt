package com.example.elixir.recipe.repository

import com.example.elixir.recipe.data.dao.CommentDao
import com.example.elixir.recipe.data.entity.CommentEntity
import com.example.elixir.recipe.data.entity.toEntity
import com.example.elixir.recipe.data.entity.toRequest
import com.example.elixir.recipe.network.api.CommentApi
import com.example.elixir.network.GetStringResponse
import com.example.elixir.recipe.data.CommentItem
import com.example.elixir.recipe.network.response.GetCommentResponse
import com.google.gson.JsonParseException
import java.io.IOException

class CommentRepository(private val commentApi: CommentApi, private val commentDao: CommentDao) {
    // 댓글 불러오기
    suspend fun getComments(recipeId: Int): Result<List<CommentItem>> {
        return try {
            val response = commentApi.getComment(recipeId)

            // 응답 코드 별 대응
            when (response.code()) {
                // 성공 시 응답 데이터가 있다면 반환, 없으면 등록된 댓글이 없다는 텍스트 반환.
                200 -> {
                    val responseData = response.body()
                    if (responseData?.data != null) Result.success(responseData.data)
                    else Result.failure(Exception("등록된 댓글이 없습니다."))
                }
                // 응답 실패 시 나타날 수 있는 주요 코드 별 대응
                401 -> Result.failure(Exception("로그인 세션이 만료되었습니다. 다시 로그인해주세요."))
                403 -> Result.failure(Exception("댓글 조회 권한이 없습니다."))
                404 -> Result.failure(Exception("레시피가 존재하지 않습니다."))
                500 -> Result.failure(Exception("서버에 오류가 발생했습니다."))
                else -> Result.failure(Exception("알 수 없는 오류가 발생했습니다: ${response.code()}: ${response.message()}"))
            }
        // 예외 처리
        } catch (e: Exception) {
            when (e) {
                is IOException -> Result.failure(Exception("네트워크 연결에 문제가 있습니다."))
                is JsonParseException -> Result.failure(Exception("서버 응답 처리 중 오류가 발생했습니다."))
                else -> Result.failure(Exception(e.localizedMessage))
            }
        }
    }

    // 댓글 업로드 (Room 저장 후 서버로 전송)
    suspend fun uploadComment(recipeId: Int, content: String): Result<GetCommentResponse> {
        val comment = CommentEntity(0, recipeId, content)
        val request = comment.toRequest()

        return try {
            val response = commentApi.uploadComment(recipeId, request)
            if (response.isSuccessful) {
                val savedComment = response.body()?.toEntity()
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