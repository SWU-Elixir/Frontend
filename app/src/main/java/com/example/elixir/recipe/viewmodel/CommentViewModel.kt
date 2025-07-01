package com.example.elixir.recipe.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elixir.recipe.data.CommentItem
import com.example.elixir.recipe.data.CommentRepository
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class CommentViewModel(private val repository: CommentRepository) : ViewModel() {
    private var _comments = MutableLiveData<List<CommentItem>?>()
    var comments: LiveData<List<CommentItem>?> = _comments

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // 댓글 불러오기
    fun fetchComments(recipeId: Int) {
        viewModelScope.launch {
            val result = repository.getComments(recipeId)
            result.onSuccess { commentList ->
                _comments.value = commentList.data
            }.onFailure { throwable ->
                _error.value = throwable.message ?: "알 수 없는 에러가 발생했습니다."
            }
        }
    }

    // 댓글 업로드
    fun uploadComment(recipeId: Int, content: String) = viewModelScope.launch {
        repository.uploadComment(recipeId, content).onSuccess { response ->
                // 서버에서 받은 댓글 데이터를 리스트에 추가
                val newComment = CommentItem(
                    commentId = response.data!!.commentId,
                    recipeId = response.data.recipeId,
                    title = response.data.title,
                    nickName = response.data.nickName,
                    authorProfileUrl = response.data.authorProfileUrl,
                    content = response.data.content,
                    createdAt = response.data.createdAt,
                    updatedAt = response.data.updatedAt
                )
                addComment(newComment)
            }
            .onFailure { e ->
                Log.e("CommentViewmodel", "Failed to upload comment: ${e.message}")
            }
    }

    private fun addComment(newComment: CommentItem) {
        val currentList = _comments.value ?: emptyList()
        _comments.value = currentList + newComment
        Log.d("CommentViewModel", "addComment: ${_comments.value}")
    }

    // 수정 함수
    fun updateComment(recipeId: Int, commentId: Int, content: String) = viewModelScope.launch {
        repository.updateComment(recipeId, commentId, content)
            .onSuccess { response ->
                // 서버에서 받은 응답으로 댓글 리스트 갱신
                val currentList = _comments.value ?: emptyList()

                val updatedList = currentList.map { comment ->
                    if (comment.commentId == commentId) {
                        comment.copy(content = content, updatedAt = LocalDateTime.now().toString())
                    } else {
                        comment
                    }
                }
                _comments.value = updatedList
            }
            .onFailure { e ->
                Log.e("CommentViewmodel", "Failed to update comment: ${e.message}")
            }
    }

    // 댓글 삭제
    fun deleteComment(recipeId: Int, commentId: Int) = viewModelScope.launch {
        repository.deleteComment(recipeId, commentId)
            .onSuccess {
                // 삭제 성공 시 댓글 목록 갱신
                val currentList = _comments.value ?: emptyList()
                _comments.value = currentList.filter { it.commentId != commentId }
            }
            .onFailure { e ->
                Log.e("CommentViewmodel", "Failed to delete comment: ${e.message}")
            }
    }
}