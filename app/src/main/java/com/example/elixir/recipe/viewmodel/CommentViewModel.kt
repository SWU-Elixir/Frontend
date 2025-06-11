package com.example.elixir.recipe.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elixir.recipe.data.CommentItem
import com.example.elixir.recipe.data.CommentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommentViewModel(private val repository: CommentRepository) : ViewModel() {
    private var _comments = MutableLiveData<List<CommentItem>>()
    var comments: LiveData<List<CommentItem>> = _comments

    private var _uiState = MutableLiveData<CommentUiState>()
    var uiState: LiveData<CommentUiState> = _uiState

    // 댓글 업로드
    fun uploadComment(recipeId: Int, content: String) = viewModelScope.launch {
        _uiState.value = CommentUiState.Loading
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
                Log.d("CommentViewModel", "uploadComment 성공, addComment 호출 전")
                addComment(newComment)
                Log.d("CommentViewModel", "uploadComment 성공, addComment 호출 후")
                _uiState.value = CommentUiState.Success(response)
            }
            .onFailure { e ->
                _uiState.value = CommentUiState.Error(e.message ?: "Failed to upload comment")
            }
    }

    private fun addComment(newComment: CommentItem) {
        val currentList = _comments.value ?: emptyList()
        _comments.value = currentList + newComment
        Log.d("CommentViewModel", "addComment: ${_comments.value}")
    }

    // 수정 함수
    fun updateComment(recipeId: Int, commentId: Int, content: String) = viewModelScope.launch {
        _uiState.value = CommentUiState.Loading
        repository.updateComment(recipeId, commentId, content)
            .onSuccess { response ->
                // 서버에서 받은 응답으로 댓글 리스트 갱신
                val currentList = _comments.value ?: emptyList()
                val updatedList = currentList.map { comment ->
                    if (comment.commentId == commentId) {
                        comment.copy(content = content, updatedAt = response.data!!.updatedAt)
                    } else {
                        comment
                    }
                }
                _comments.value = updatedList
                _uiState.value = CommentUiState.Success(response)
            }
            .onFailure { e ->
                _uiState.value = CommentUiState.Error(e.message ?: "Failed to update comment")
            }
    }

    // 댓글 삭제
    fun deleteComment(recipeId: Int, commentId: Int) = viewModelScope.launch {
        _uiState.value = CommentUiState.Loading
        repository.deleteComment(recipeId, commentId)
            .onSuccess { response ->
                // 삭제 성공 시 댓글 목록 갱신
                val currentList = _comments.value ?: emptyList()
                _comments.value = currentList.filter { it.commentId != commentId }
                _uiState.value = CommentUiState.Success(response)
            }
            .onFailure { e ->
                _uiState.value = CommentUiState.Error(e.message ?: "Failed to delete comment")
            }
    }

}

sealed class CommentUiState {
    object Idle : CommentUiState()
    object Loading : CommentUiState()
    data class Success<T>(val data: T) : CommentUiState()
    data class Error(val message: String) : CommentUiState()
}