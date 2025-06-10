package com.example.elixir.recipe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elixir.recipe.data.CommentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommentViewModel(private val repository: CommentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<CommentUiState>(CommentUiState.Idle)
    val uiState: StateFlow<CommentUiState> = _uiState

    // 댓글 업로드
    fun uploadComment(recipeId: Int, content: String) = viewModelScope.launch {
        _uiState.value = CommentUiState.Loading
        repository.uploadComment(recipeId, content)
            .onSuccess { response ->
                _uiState.value = CommentUiState.Success(response)
            }
            .onFailure { e ->
                _uiState.value = CommentUiState.Error(e.message ?: "Failed to upload comment")
            }
    }

    // 댓글 수정
    fun updateComment(recipeId: Int, commentId: Int, content: String) = viewModelScope.launch {
        _uiState.value = CommentUiState.Loading
        repository.updateComment(recipeId, commentId, content)
            .onSuccess { response ->
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